package com.gnl.workhub.backend.controller;

import com.gnl.workhub.backend.dto.ProjectRequest;
import com.gnl.workhub.backend.dto.ProjectResponse;
import com.gnl.workhub.backend.dto.UpdateProjectRequest;
import com.gnl.workhub.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public List<ProjectResponse> getProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable UUID id) {
        return projectService.getProjectById(id);
    }

    @PostMapping
    public ProjectResponse createProject(@RequestBody ProjectRequest request) {
        return projectService.createProject(request);
    }

    @GetMapping("/own")
    public List<ProjectResponse> getMyProjects() {
        return projectService.getMyProjects();
    }

    @PatchMapping("/{projectId}")
    public ProjectResponse updateProject(
            @PathVariable UUID projectId,
            @RequestBody UpdateProjectRequest request) {
        return projectService.updateProject(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    public void deleteProject(@PathVariable UUID projectId) {
        projectService.deleteProject(projectId);
    }
}

