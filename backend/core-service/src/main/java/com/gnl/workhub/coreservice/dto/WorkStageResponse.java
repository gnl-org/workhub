package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.SprintStatus;
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
