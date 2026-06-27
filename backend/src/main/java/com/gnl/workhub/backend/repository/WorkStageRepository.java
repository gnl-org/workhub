package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.WorkStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkStageRepository extends JpaRepository<WorkStage, UUID> {

    List<WorkStage> findByProjectIdOrderBySortOrderAsc(UUID projectId);

    Optional<WorkStage> findBySprintId(UUID sprintId);

    boolean existsByProjectIdAndName(UUID projectId, String name);

    @Query("SELECT w FROM WorkStage w WHERE w.project.id = :projectId AND w.name = 'Backlog' AND w.sprint IS NULL")
    Optional<WorkStage> findDefaultBacklogStage(@Param("projectId") UUID projectId);
}
