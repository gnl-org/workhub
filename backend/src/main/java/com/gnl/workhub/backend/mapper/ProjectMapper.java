package com.gnl.workhub.backend.mapper;

import com.gnl.workhub.backend.dto.ProjectRequest;
import com.gnl.workhub.backend.dto.ProjectResponse;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    // Map Request -> Entity
    public Project toEntity(ProjectRequest request, User owner) {
        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        // Status is usually handled by @PrePersist or default value in Entity
        return project;
    }

    // Map Entity -> Response
    public ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setTitle(project.getTitle());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus());

        // Handle potential nulls for safety
        if (project.getOwner() != null) {
            response.setOwnerName(project.getOwner().getFullName());
        }

        return response;
    }
}