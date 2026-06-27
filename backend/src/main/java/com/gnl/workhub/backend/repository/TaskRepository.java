package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.enums.TaskPriority;
import com.gnl.workhub.backend.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {
    // OVERRIDE the findAll specification executor to join your associations
    @Override
    @EntityGraph(attributePaths = {"assignedTo", "owner", "project", "workStage", "sprint"})
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);

    // Find all tasks in a project
    List<Task> findByProjectId(UUID projectId);

    // Find all tasks assigned to a user
    List<Task> findByAssignedToId(UUID assignedToId);

    // Find all tasks assigned to a user in a specific project
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.assignedTo.id = :userId")
    List<Task> findByProjectIdAndAssignedToId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    // ⚡ OPTIMIZED: The EntityGraph collapses your N+1 select loop into a single step
    @EntityGraph(attributePaths = {"assignedTo", "owner", "project"})
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
            "AND t.deleted = false " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:assigneeId IS NULL OR t.assignedTo.id = :assigneeId) " +
            "AND (CAST(:searchTerm AS text) IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS text), '%'))) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR t.dueDate >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR t.dueDate <= :endDate)")
    Page<Task> findAdvancedFilteredTasks(
            @Param("projectId") UUID projectId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("assigneeId") UUID assigneeId,
            @Param("searchTerm") String searchTerm,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Ensure users can't find a deleted task by its direct ID
    Optional<Task> findByIdAndDeletedFalse(UUID id);

    long countByProjectId(UUID projectId);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> countTasksByStatus(UUID projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId " +
            "AND t.dueDate < CURRENT_TIMESTAMP AND t.status != 'COMPLETED'")
    long countOverdueTasks(UUID projectId);

    @EntityGraph(attributePaths = {"assignedTo", "owner", "project", "workStage", "sprint"})
    List<Task> findByWorkStageIdAndDeletedFalseOrderBySortOrderAsc(UUID workStageId);

    List<Task> findBySprintIdAndDeletedFalse(UUID sprintId);

    long countBySprintIdAndStatusNotIn(UUID sprintId, List<TaskStatus> statuses);

    @Query("SELECT MAX(t.sortOrder) FROM Task t WHERE t.workStage.id = :workStageId")
    Optional<Integer> findMaxSortOrderByWorkStageId(@Param("workStageId") UUID workStageId);
}