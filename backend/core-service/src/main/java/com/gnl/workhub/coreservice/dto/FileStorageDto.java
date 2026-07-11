package com.gnl.workhub.coreservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public sealed interface FileStorageDto {

    record File(
            UUID id,
            String fileName,
            String mimeType,
            long fileSize,
            String uploadedByName,
            LocalDateTime createdAt,
            String url
    ) implements FileStorageDto {}
}
