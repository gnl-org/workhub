package com.gnl.workhub.coreservice.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageProvider {
    void init();
    String store(String storedName, MultipartFile file);
    Resource load(String filePath);
    void delete(String filePath);
}
