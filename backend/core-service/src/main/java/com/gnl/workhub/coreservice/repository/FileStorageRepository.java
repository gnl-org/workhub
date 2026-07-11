package com.gnl.workhub.coreservice.repository;

import com.gnl.workhub.coreservice.entity.FileStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, UUID> {

    List<FileStorage> findByTaskIdOrderByCreatedAtDesc(UUID taskId);
}
