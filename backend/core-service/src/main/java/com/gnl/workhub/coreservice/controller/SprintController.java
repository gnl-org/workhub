package com.gnl.workhub.coreservice.controller;

import com.gnl.workhub.coreservice.dto.*;
import com.gnl.workhub.coreservice.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SprintController {

    private final SprintService sprintService;

    @GetMapping("/projects/{projectId}/sprints")
    public List<SprintResponse> listSprints(@PathVariable UUID projectId) {
        return sprintService.listSprints(projectId);
    }

    @GetMapping("/projects/{projectId}/sprints/active")
    public SprintResponse getActiveSprint(@PathVariable UUID projectId) {
        return sprintService.getActiveSprint(projectId);
    }

    @GetMapping("/projects/{projectId}/sprints/history")
    public List<SprintResponse> listClosedSprints(@PathVariable UUID projectId) {
        return sprintService.listClosedSprints(projectId);
    }

    @GetMapping("/projects/{projectId}/sprints/{sprintId}")
    public SprintDetailResponse getSprintDetail(@PathVariable UUID sprintId) {
        return sprintService.getSprintDetail(sprintId);
    }

    @PostMapping("/projects/{projectId}/sprints")
    public SprintResponse createSprint(@PathVariable UUID projectId,
                                        @RequestBody CreateSprintRequest request) {
        return sprintService.createSprint(projectId, request);
    }

    @PatchMapping("/projects/{projectId}/sprints/{sprintId}")
    public SprintResponse updateSprint(@PathVariable UUID sprintId,
                                        @RequestBody UpdateSprintRequest request) {
        return sprintService.updateSprint(sprintId, request);
    }

    @PostMapping("/projects/{projectId}/sprints/{sprintId}/start")
    public SprintResponse startSprint(@PathVariable UUID sprintId) {
        return sprintService.startSprint(sprintId);
    }

    @PostMapping("/projects/{projectId}/sprints/{sprintId}/close")
    public CloseSprintResponse closeSprint(@PathVariable UUID sprintId) {
        return sprintService.closeSprint(sprintId);
    }

    @PostMapping("/projects/{projectId}/sprints/{sprintId}/tasks")
    public ResponseEntity<Void> assignTasks(@PathVariable UUID sprintId,
                                             @RequestBody AssignTasksRequest request) {
        sprintService.assignTasks(sprintId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/projects/{projectId}/sprints/{sprintId}/tasks")
    public ResponseEntity<Void> removeTasks(@PathVariable UUID sprintId,
                                             @RequestBody AssignTasksRequest request) {
        sprintService.removeTasks(sprintId, request);
        return ResponseEntity.ok().build();
    }
}
