package com.gnl.workhub.coreservice.controller;

import com.gnl.workhub.coreservice.dto.*;
import com.gnl.workhub.coreservice.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BacklogController {

    private final TaskService taskService;

    @GetMapping("/projects/{projectId}/backlog")
    public BacklogResponse getBacklog(@PathVariable UUID projectId) {
        return taskService.getBacklog(projectId);
    }

    @PatchMapping("/projects/{projectId}/tasks/{taskId}/move")
    public TaskResponse moveTask(@PathVariable UUID projectId,
                                  @PathVariable UUID taskId,
                                  @RequestBody MoveTaskRequest request) {
        return taskService.moveTask(projectId, taskId, request);
    }

    @PutMapping("/projects/{projectId}/work-stages/{stageId}/tasks/reorder")
    public ResponseEntity<Void> reorderTasks(@PathVariable UUID projectId,
                                              @PathVariable UUID stageId,
                                              @RequestBody ReorderTasksRequest request) {
        taskService.reorderTasksInStage(projectId, stageId, request);
        return ResponseEntity.ok().build();
    }
}
