package com.gnl.workhub.coreservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class SprintDetailResponse {
    private SprintResponse sprint;
    private List<TaskResponse> tasks;
}
