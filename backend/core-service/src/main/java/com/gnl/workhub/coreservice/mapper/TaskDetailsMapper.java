package com.gnl.workhub.coreservice.mapper;

import com.gnl.workhub.coreservice.dto.TaskCommentDto;
import com.gnl.workhub.coreservice.dto.TaskDetailsResponse;
import com.gnl.workhub.coreservice.entity.Task;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskDetailsMapper {

    // Map Entity -> Response
    public TaskDetailsResponse toResponse(Task task) {
        List<TaskCommentDto.Response> commentDtos = task.getComments().stream()
                .map(c -> new TaskCommentDto.Response(
                        c.getId(),
                        c.getContent(),
                        c.getAuthor().getFullName(),
                        c.getAuthor().getId(),
                        c.getCreatedAt()
                )).toList();

        TaskDetailsResponse.UserSummary assignee = task.getAssignedTo() != null
                ? new TaskDetailsResponse.UserSummary(
                        task.getAssignedTo().getId(),
                        task.getAssignedTo().getFullName(),
                        task.getAssignedTo().getEmail())
                : null;

        TaskDetailsResponse response = new TaskDetailsResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getTaskType(),
                task.getStoryPoints(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getProject().getTitle(),
                assignee,
                new TaskDetailsResponse.UserSummary(
                        task.getOwner().getId(),
                        task.getOwner().getFullName(),
                        task.getOwner().getEmail()
                ),
                commentDtos,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );

        return response;
    }
}