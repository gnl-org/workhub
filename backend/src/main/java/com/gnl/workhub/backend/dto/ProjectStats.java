package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.TaskPriority;
import com.gnl.workhub.backend.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ProjectStats {
    private long totalTasks;
    private Map<TaskStatus, Long> statusCounts;
    private Map<TaskPriority, Long> priorityCounts;
    private long overdueTasks;
}
