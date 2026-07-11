package com.gnl.workhub.coreservice.service;

import com.gnl.workhub.coreservice.dto.FileStorageDto;
import com.gnl.workhub.coreservice.entity.FileStorage;
import com.gnl.workhub.coreservice.entity.Task;
import com.gnl.workhub.coreservice.exception.ResourceNotFoundException;
import com.gnl.workhub.coreservice.repository.FileStorageRepository;
import com.gnl.workhub.coreservice.repository.TaskRepository;
import com.gnl.workhub.coreservice.storage.StorageProvider;
import com.gnl.workhub.coreservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
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

        return new FileStorageDto.File(
                saved.getId(),
                saved.getFileName(),
                saved.getMimeType(),
                saved.getFileSize(),
                currentUser.getFullName(),
                saved.getCreatedAt()
        );
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

        return storageProvider.load(fileStorage.getFilePath());
    }

    @Transactional
    public void deleteFile(UUID fileId) {
        FileStorage fileStorage = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        storageProvider.delete(fileStorage.getFilePath());
        fileStorageRepository.delete(fileStorage);
    }
}
