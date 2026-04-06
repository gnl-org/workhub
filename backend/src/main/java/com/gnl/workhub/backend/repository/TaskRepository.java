package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    // Find all tasks in a project
    List<Task> findByProjectId(UUID projectId);
    
    // Find all tasks assigned to a user
    List<Task> findByAssignedToId(UUID assignedToId);
    
    // Find all tasks assigned to a user in a specific project
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.assignedTo.id = :userId")
    List<Task> findByProjectIdAndAssignedToId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
