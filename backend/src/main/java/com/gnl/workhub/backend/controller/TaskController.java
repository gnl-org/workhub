package com.gnl.workhub.backend.controller;

import com.gnl.workhub.backend.dto.*;
import com.gnl.workhub.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/projects/{projectId}/tasks")
    public TaskResponse createTask(@PathVariable UUID projectId, @RequestBody TaskRequest request) {
        return taskService.createTask(projectId, request);
    }

    @GetMapping("/projects/{projectId}/tasks/{taskId}")
    public TaskDetailsResponse getTaskById(@PathVariable UUID projectId, @PathVariable UUID taskId) {
        return taskService.getTaskById(projectId, taskId);
    }

    @PatchMapping("/projects/{projectId}/tasks/{taskId}")
    public TaskResponse updateTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(projectId, taskId, request);
    }

    @DeleteMapping("/projects/{projectId}/tasks/{taskId}")
    public void deleteTask(@PathVariable UUID projectId, @PathVariable UUID taskId) {
        taskService.deleteTask(projectId, taskId);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @PathVariable UUID projectId,
            TaskFilterRequest filters,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(taskService.getTasksByProjectId(
                projectId, filters, pageable
        ));
    }
}
