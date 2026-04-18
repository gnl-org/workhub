package com.gnl.workhub.backend.mapper;

import com.gnl.workhub.backend.dto.TaskCommentDto;
import com.gnl.workhub.backend.dto.TaskDetailsResponse;
import com.gnl.workhub.backend.entity.Task;
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

        TaskDetailsResponse response = new TaskDetailsResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getProject().getTitle(),
                new TaskDetailsResponse.UserSummary(
                        task.getAssignedTo().getId(),
                        task.getAssignedTo().getFullName(),
                        task.getAssignedTo().getEmail()
                ),
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