package com.gnl.workhub.coreservice.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageProvider implements StorageProvider {

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    @PostConstruct
    @Override
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    @Override
    public String store(String storedName, MultipartFile file) {
        try {
            Path targetPath = Paths.get(uploadDir, storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return storedName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + storedName, e);
        }
    }

    @Override
    public Resource load(String filePath) {
        try {
            Path path = Paths.get(uploadDir).resolve(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("File not found on disk: " + filePath);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Path path = Paths.get(uploadDir).resolve(filePath).normalize();
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}
    }

    @Override
    public String generateUrl(String filePath, String fileName) {
        return null;
    }
}
