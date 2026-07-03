package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.SprintStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SprintResponse {
    private UUID id;
    private String name;
    private String goal;
    private SprintStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant closedAt;
    private int totalTasks;
    private int completedTasks;
    private int incompleteTasks;
}
