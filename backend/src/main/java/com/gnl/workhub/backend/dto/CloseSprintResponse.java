package com.gnl.workhub.backend.dto;

import lombok.Data;

@Data
public class CloseSprintResponse {
    private int totalIncompleteTasks;
    private int movedToSprintTasks;
    private String targetSprintName;
}
