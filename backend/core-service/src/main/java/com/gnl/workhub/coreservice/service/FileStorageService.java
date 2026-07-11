package com.gnl.workhub.coreservice.service;

import com.gnl.workhub.coreservice.dto.FileResource;
import com.gnl.workhub.coreservice.dto.FileStorageDto;
import com.gnl.workhub.coreservice.entity.FileStorage;
import com.gnl.workhub.coreservice.entity.Task;
import com.gnl.workhub.coreservice.exception.ResourceNotFoundException;
import com.gnl.workhub.coreservice.repository.FileStorageRepository;
import com.gnl.workhub.coreservice.repository.TaskRepository;
import com.gnl.workhub.coreservice.storage.StorageProvider;
import com.gnl.workhub.coreservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageRepository fileStorageRepository;
    private final TaskRepository taskRepository;
    private final SecurityUtil securityUtil;
    private final StorageProvider storageProvider;

    private String resolveUrl(FileStorage fileStorage) {
        String url = storageProvider.generateUrl(fileStorage.getFilePath(), fileStorage.getFileName());
        return url != null ? url : "/api/v1/files/" + fileStorage.getId() + "/download";
    }

    private FileStorageDto.File toDto(FileStorage fileStorage, String uploadedByName) {
        return new FileStorageDto.File(
                fileStorage.getId(),
                fileStorage.getFileName(),
                fileStorage.getMimeType(),
                fileStorage.getFileSize(),
                uploadedByName,
                fileStorage.getCreatedAt(),
                resolveUrl(fileStorage)
        );
    }

    @Transactional
    public FileStorageDto.File uploadFile(UUID taskId, MultipartFile file) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        var currentUser = securityUtil.getCurrentUser();

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID() + extension;

        storageProvider.store(storedName, file);

        FileStorage fileStorage = FileStorage.builder()
                .fileName(originalName)
                .filePath(storedName)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .task(task)
                .uploadedBy(currentUser)
                .build();

        FileStorage saved = fileStorageRepository.save(fileStorage);

        return toDto(saved, currentUser.getFullName());
    }

    @Transactional(readOnly = true)
    public List<FileStorageDto.File> getFiles(UUID taskId) {
        return fileStorageRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(f -> toDto(f, f.getUploadedBy().getFullName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public FileResource downloadFile(UUID fileId) {
        FileStorage fileStorage = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        return new FileResource(
                storageProvider.load(fileStorage.getFilePath()),
                fileStorage.getMimeType(),
                fileStorage.getFileName()
        );
    }

    @Transactional
    public void deleteFile(UUID fileId) {
        FileStorage fileStorage = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        storageProvider.delete(fileStorage.getFilePath());
        fileStorageRepository.delete(fileStorage);
    }
}
