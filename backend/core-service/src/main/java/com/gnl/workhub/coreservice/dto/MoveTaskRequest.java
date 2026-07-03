package com.gnl.workhub.coreservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class MoveTaskRequest {
    private UUID workStageId;
    private Integer sortOrder;
}
