package com.gnl.workhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWorkStageRequest {
    @NotBlank
    private String name;
}
