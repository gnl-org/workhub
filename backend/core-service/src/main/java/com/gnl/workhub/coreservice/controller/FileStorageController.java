package com.gnl.workhub.coreservice.controller;

import com.gnl.workhub.coreservice.dto.FileResource;
import com.gnl.workhub.coreservice.dto.FileStorageDto;
import com.gnl.workhub.coreservice.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/api/v1/projects/{projectId}/tasks/{taskId}/files")
    public ResponseEntity<FileStorageDto.File> uploadFile(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileStorageService.uploadFile(taskId, file));
    }

    @GetMapping("/api/v1/projects/{projectId}/tasks/{taskId}/files")
    public ResponseEntity<List<FileStorageDto.File>> getFiles(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {

        return ResponseEntity.ok(fileStorageService.getFiles(taskId));
    }

    @GetMapping("/api/v1/files/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        FileResource fr = fileStorageService.downloadFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        fr.contentType() != null ? fr.contentType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fr.filename() + "\"")
                .body(fr.resource());
    }

    @DeleteMapping("/api/v1/files/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        fileStorageService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
