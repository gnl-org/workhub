package com.gnl.workhub.backend.mapper;

import com.gnl.workhub.backend.dto.TaskRequest;
import com.gnl.workhub.backend.dto.TaskResponse;
import com.gnl.workhub.backend.dto.UpdateTaskRequest;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    // Map Request -> Entity
    public Task toEntity(TaskRequest request, Project project, User assignee) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setAssignedTo(assignee);
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        return task;
    }

    // Partial update - Map UpdateRequest -> Entity (only update non-null fields)
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
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        // For assignedTo, we allow it to be set to null (unassign)
        task.setAssignedTo(assignee);
    }

    // Map Entity -> Response
    public TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        // Handle Project info
        if (task.getProject() != null) {
            response.setProjectTitle(task.getProject().getTitle());
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