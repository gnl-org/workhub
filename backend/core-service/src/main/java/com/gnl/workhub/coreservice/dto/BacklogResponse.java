package com.gnl.workhub.coreservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class BacklogResponse {
    private List<BacklogStageDto> stages;
}
