package com.gnl.workhub.coreservice.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AssignTasksRequest {
    private List<UUID> taskIds;
}
