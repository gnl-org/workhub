package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private UUID id;
    private String title;
    private String description;
    private ProjectStatus status;
    private String ownerName; // Flat data is better for frontend than nested objects
}