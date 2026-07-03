package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.ProjectStatus;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    private String title;
    private String description;
    private ProjectStatus status;
}
