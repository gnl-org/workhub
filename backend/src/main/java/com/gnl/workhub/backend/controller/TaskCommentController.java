package com.gnl.workhub.backend.controller;

import com.gnl.workhub.backend.dto.TaskCommentDto;
import com.gnl.workhub.backend.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService commentService;

    @PostMapping
    public ResponseEntity<TaskCommentDto.Response> addComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskCommentDto.Request request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(taskId, request));
    }
}