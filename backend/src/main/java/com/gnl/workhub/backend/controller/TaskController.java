package com.gnl.workhub.backend.controller;

import com.gnl.workhub.backend.dto.TaskRequest;
import com.gnl.workhub.backend.dto.TaskResponse;
import com.gnl.workhub.backend.dto.UpdateTaskRequest;
import com.gnl.workhub.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/project/{projectId}/tasks")
    public TaskResponse createTask(@PathVariable UUID projectId, @RequestBody TaskRequest request) {
        return taskService.createTask(projectId, request);
    }

    @GetMapping("/project/{projectId}/tasks/{taskId}")
    public TaskResponse getTaskById(@PathVariable UUID projectId, @PathVariable UUID taskId) {
        return taskService.getTaskById(projectId, taskId);
    }

    @PatchMapping("/project/{projectId}/tasks/{taskId}")
    public TaskResponse updateTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(projectId, taskId, request);
    }

    @DeleteMapping("/project/{projectId}/tasks/{taskId}")
    public void deleteTask(@PathVariable UUID projectId, @PathVariable UUID taskId) {
        taskService.deleteTask(projectId, taskId);
    }
}
