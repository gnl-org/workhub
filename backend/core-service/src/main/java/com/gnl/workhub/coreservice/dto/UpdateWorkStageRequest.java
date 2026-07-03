package com.gnl.workhub.coreservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateWorkStageRequest {
    @NotBlank
    private String name;
}
