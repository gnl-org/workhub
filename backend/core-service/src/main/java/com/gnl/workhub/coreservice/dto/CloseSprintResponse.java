package com.gnl.workhub.coreservice.dto;

import lombok.Data;

@Data
public class CloseSprintResponse {
    private int totalIncompleteTasks;
    private int movedToSprintTasks;
    private String targetSprintName;
}
