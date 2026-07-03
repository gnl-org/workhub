package com.gnl.workhub.coreservice.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ProjectRequest {
    private String title;
    private String description;
}