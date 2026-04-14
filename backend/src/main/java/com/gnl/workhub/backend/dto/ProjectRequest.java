package com.gnl.workhub.backend.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ProjectRequest {
    private String title;
    private String description;
}