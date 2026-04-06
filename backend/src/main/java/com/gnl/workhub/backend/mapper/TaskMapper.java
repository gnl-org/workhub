package com.gnl.workhub.backend.mapper;

import com.gnl.workhub.backend.dto.TaskRequest;
import com.gnl.workhub.backend.dto.TaskResponse;
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
        task.setProject(project);
        task.setAssignedTo(assignee);
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        return task;
    }

    // Map Entity -> Response
    public TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());

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