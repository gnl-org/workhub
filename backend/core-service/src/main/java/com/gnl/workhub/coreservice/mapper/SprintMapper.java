package com.gnl.workhub.coreservice.mapper;

import com.gnl.workhub.coreservice.dto.SprintResponse;
import com.gnl.workhub.coreservice.entity.Sprint;
import org.springframework.stereotype.Component;

@Component
public class SprintMapper {

    public SprintResponse toResponse(Sprint sprint) {
        SprintResponse response = new SprintResponse();
        response.setId(sprint.getId());
        response.setName(sprint.getName());
        response.setGoal(sprint.getGoal());
        response.setStatus(sprint.getStatus());
        response.setStartDate(sprint.getStartDate());
        response.setEndDate(sprint.getEndDate());
        response.setClosedAt(sprint.getClosedAt());
        return response;
    }
}
