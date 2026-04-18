package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    /**
     * Fetches all comments for a specific task,
     * ordered by creation date (newest first).
     */
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    @Query("SELECT c FROM TaskComment c JOIN FETCH c.author WHERE c.task.id = :taskId ORDER BY c.createdAt DESC")
    List<TaskComment> findByTaskIdWithAuthor(UUID taskId);

    /**
     * Optional: Counts comments for a task
     * (useful for showing a "comment count" badge in the UI).
     */
    long countByTaskId(UUID taskId);
}