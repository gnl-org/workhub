package com.gnl.workhub.coreservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWorkStageRequest {
    @NotBlank
    private String name;
}
