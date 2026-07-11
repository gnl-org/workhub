package com.gnl.workhub.coreservice.storage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
@RequiredArgsConstructor
public class S3StorageProvider implements StorageProvider {

    private final S3Client s3Client;

    @Value("${app.storage.s3.bucket}")
    private String bucketName;

    @PostConstruct
    @Override
    public void init() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }

    @Override
    public String store(String storedName, MultipartFile file) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return storedName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file in S3: " + storedName, e);
        }
    }

    @Override
    public Resource load(String filePath) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();
        try {
            var s3Object = s3Client.getObject(request);
            return new InputStreamResource(s3Object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file from S3: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();
        s3Client.deleteObject(request);
    }

    @Override
    public String generateUrl(String filePath, String fileName) {
        var request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .responseContentDisposition("inline; filename=\"" + fileName + "\"")
                .build();

        var presigner = S3Presigner.builder()
                .region(s3Client.serviceClientConfiguration().region())
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                .build();

        var presigned = presigner.presignGetObject(pr ->
                pr.getObjectRequest(request)
                        .signatureDuration(Duration.ofHours(1)));

        presigner.close();
        return presigned.url().toString();
    }
}
