package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.SprintStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class WorkStageResponse {
    private UUID id;
    private String name;
    private int sortOrder;
    private UUID sprintId;
    private SprintStatus sprintStatus;
}
