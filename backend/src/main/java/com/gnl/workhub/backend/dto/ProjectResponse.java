package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.ProjectStatus;
import lombok.Data;
import java.util.UUID;

@Data
public class ProjectResponse {
    private UUID id;
    private String title;
    private String description;
    private ProjectStatus status;
    private String ownerName; // Flat data is better for frontend than nested objects
}