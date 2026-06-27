package com.gnl.workhub.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class BacklogResponse {
    private List<BacklogStageDto> stages;
}
