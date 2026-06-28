package com.gnl.workhub.backend.mapper;

import com.gnl.workhub.backend.dto.TaskRequest;
import com.gnl.workhub.backend.dto.TaskResponse;
import com.gnl.workhub.backend.dto.UpdateTaskRequest;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.entity.WorkStage;
import com.gnl.workhub.backend.enums.SprintStatus;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public Task toEntity(TaskRequest request, Project project, User assignee, User creator) {
        Task.TaskBuilder builder = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .assignedTo(assignee)
                .owner(creator)
                .status(request.getStatus())
                .priority(request.getPriority())
                .taskType(request.getTaskType())
                .storyPoints(request.getStoryPoints())
                .dueDate(request.getDueDate());

        if (request.getWorkStageId() != null) {
            WorkStage ws = new WorkStage();
            ws.setId(request.getWorkStageId());
            builder.workStage(ws);
        }

        return builder.build();
    }

    public void updateEntityFromRequest(UpdateTaskRequest request, Task task, User assignee) {
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getTaskType() != null) {
            task.setTaskType(request.getTaskType());
        }
        if (request.getStoryPoints() != null) {
            task.setStoryPoints(request.getStoryPoints());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        task.setAssignedTo(assignee);
    }

    public TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setTaskType(task.getTaskType());
        response.setStoryPoints(task.getStoryPoints());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setOwner(task.getOwner().getId());
        response.setProjectTitle(task.getProject().getId());
        response.setSortOrder(task.getSortOrder());

        if (task.getWorkStage() != null) {
            response.setWorkStageId(task.getWorkStage().getId());
        }

        if (task.getSprint() != null) {
            response.setSprintId(task.getSprint().getId());
            response.setInActiveSprint(task.getSprint().getStatus() == SprintStatus.ACTIVE);
        }

        // Handle Assignee info for the "assigneeName" field
        if (task.getAssignedTo() != null) {
            String fullName = task.getAssignedTo().getFullName();
            response.setAssigneeName(fullName);
        } else {
            response.setAssigneeName("Unassigned");
        }

        return response;
    }
}