package com.gnl.workhub.coreservice.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReorderTasksRequest {
    private List<UUID> taskIds;
}
