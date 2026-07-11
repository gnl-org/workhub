package com.gnl.workhub.coreservice.service;

import com.gnl.workhub.coreservice.dto.FileStorageDto;
import com.gnl.workhub.coreservice.entity.FileStorage;
import com.gnl.workhub.coreservice.entity.Task;
import com.gnl.workhub.coreservice.entity.User;
import com.gnl.workhub.coreservice.exception.ResourceNotFoundException;
import com.gnl.workhub.coreservice.repository.FileStorageRepository;
import com.gnl.workhub.coreservice.repository.TaskRepository;
import com.gnl.workhub.coreservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageRepository fileStorageRepository;
    private final TaskRepository taskRepository;
    private final SecurityUtil securityUtil;

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    @Transactional
    public FileStorageDto.File uploadFile(UUID taskId, MultipartFile file) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User currentUser = securityUtil.getCurrentUser();

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID() + extension;

        try {
            Path targetPath = Paths.get(uploadDir, storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            FileStorage fileStorage = FileStorage.builder()
                    .fileName(originalName)
                    .filePath(storedName)
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .task(task)
                    .uploadedBy(currentUser)
                    .build();

            FileStorage saved = fileStorageRepository.save(fileStorage);

            return new FileStorageDto.File(
                    saved.getId(),
                    saved.getFileName(),
                    saved.getMimeType(),
                    saved.getFileSize(),
                    currentUser.getFullName(),
                    saved.getCreatedAt()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalName, e);
        }
    }

    @Transactional(readOnly = true)
    public List<FileStorageDto.File> getFiles(UUID taskId) {
        return fileStorageRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(f -> new FileStorageDto.File(
                        f.getId(),
                        f.getFileName(),
                        f.getMimeType(),
                        f.getFileSize(),
                        f.getUploadedBy().getFullName(),
                        f.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadFile(UUID fileId) {
        FileStorage fileStorage = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        try {
            Path filePath = Paths.get(uploadDir).resolve(fileStorage.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("File not found on disk: " + fileStorage.getFileName());
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + fileStorage.getFileName(), e);
        }
    }

    @Transactional
    public void deleteFile(UUID fileId) {
        FileStorage fileStorage = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        try {
            Path filePath = Paths.get(uploadDir).resolve(fileStorage.getFilePath()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {}

        fileStorageRepository.delete(fileStorage);
    }
}
