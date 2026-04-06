package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.ProjectStatus;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    private String title;
    private String description;
    private ProjectStatus status;
}
