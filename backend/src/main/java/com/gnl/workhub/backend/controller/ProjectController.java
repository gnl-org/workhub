package com.gnl.workhub.backend.controller;

import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.service.ProjectService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Data
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public List<Project> getProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public Project getProjectById(@PathVariable UUID id) {
        return projectService.getProjectById(id);
    }

    @PostMapping
    public  Project createProject(@RequestBody  Project project) {
        return projectService.createProject(project);
    }
}
