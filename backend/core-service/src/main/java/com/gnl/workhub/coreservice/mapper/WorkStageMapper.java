package com.gnl.workhub.coreservice.mapper;

import com.gnl.workhub.coreservice.dto.WorkStageResponse;
import com.gnl.workhub.coreservice.entity.WorkStage;
import org.springframework.stereotype.Component;

@Component
public class WorkStageMapper {

    public WorkStageResponse toResponse(WorkStage stage) {
        WorkStageResponse response = new WorkStageResponse();
        response.setId(stage.getId());
        response.setName(stage.getName());
        response.setSortOrder(stage.getSortOrder());

        if (stage.getSprint() != null) {
            response.setSprintId(stage.getSprint().getId());
            response.setSprintStatus(stage.getSprint().getStatus());
        }

        return response;
    }
}
