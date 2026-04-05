package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    // Optional: add custom queries later
}
