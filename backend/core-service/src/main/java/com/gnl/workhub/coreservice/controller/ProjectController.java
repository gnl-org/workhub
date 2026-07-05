package com.gnl.workhub.coreservice.controller;

import com.gnl.workhub.coreservice.dto.*;
import com.gnl.workhub.coreservice.service.ProjectMemberService;
import com.gnl.workhub.coreservice.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;

    // Inside your ProjectController class, add this static list:
    // private static final List<byte[]> leakedDataHolder = new ArrayList<>();

    @GetMapping
    public List<ProjectResponse> getProjects() {
        // INTENTIONAL MEMORY LEAK LAB:
        // Every single HTTP request simulates allocating roughly 1MB of memory overhead
        // byte[] simulatedPayloadData = new byte[1024 * 1024];
        // leakedDataHolder.add(simulatedPayloadData);

        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable UUID id) {
        return projectService.getProjectById(id);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ProjectStats> getProjectStats(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectStats(id));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(projectMemberService.getMembers(id));
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

