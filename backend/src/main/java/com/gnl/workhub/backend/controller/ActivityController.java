package com.gnl.workhub.backend.controller;

import com.gnl.workhub.backend.dto.ActivityLogResponse;
import com.gnl.workhub.backend.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks/{taskId}/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLogResponse>> getTaskFeed(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {
        // You could add a check here to ensure the task belongs to the project
        return ResponseEntity.ok(activityLogService.getTaskActivity(taskId));
    }
}