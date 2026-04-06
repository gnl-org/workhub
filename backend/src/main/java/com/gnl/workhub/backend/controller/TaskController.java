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
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public TaskResponse createTask(@RequestBody TaskRequest request) {
        return taskService.createTask(request);
    }

    @GetMapping("/project/{projectId}")
    public List<TaskResponse> getTasksByProject(@PathVariable UUID projectId) {
        return taskService.getTasksByProjectId(projectId);
    }

    @GetMapping("/project/{projectId}/user/{userId}")
    public List<TaskResponse> getTasksByProjectAndUser(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        return taskService.getTasksByProjectIdAndUserId(projectId, userId);
    }

    @GetMapping("/{taskId}")
    public TaskResponse getTaskById(@PathVariable UUID taskId) {
        return taskService.getTaskById(taskId);
    }

    @PatchMapping("/{taskId}")
    public TaskResponse updateTask(
            @PathVariable UUID taskId,
            @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(taskId, request);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable UUID taskId) {
        taskService.deleteTask(taskId);
    }
}
