package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    // Project Feed: All logs for a project (including all its tasks)
    List<ActivityLog> findByProjectIdOrderByTimestampDesc(UUID projectId);

    // Task History: Only logs for a specific task
    List<ActivityLog> findByTaskIdOrderByTimestampDesc(UUID taskId);
}