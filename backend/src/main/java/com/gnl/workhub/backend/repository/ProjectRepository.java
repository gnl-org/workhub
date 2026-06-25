package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    // Find all projects created/owned by a user
    List<Project> findByOwnerId(UUID ownerId);

    // Overriding or creating a method to fetch everything in ONE shot
    @Query("SELECT p FROM Project p JOIN FETCH p.owner")
    List<Project> findAllWithOwners();
}
