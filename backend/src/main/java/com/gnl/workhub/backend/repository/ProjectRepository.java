package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    // Find all projects created/owned by a user
    List<Project> findByOwnerId(UUID ownerId);
}
