package com.gnl.workhub.coreservice.service;

import com.gnl.workhub.coreservice.dto.TaskCommentDto;
import com.gnl.workhub.coreservice.entity.Task;
import com.gnl.workhub.coreservice.entity.TaskComment;
import com.gnl.workhub.coreservice.entity.User;
import com.gnl.workhub.coreservice.exception.ResourceNotFoundException;
import com.gnl.workhub.coreservice.repository.TaskCommentRepository;
import com.gnl.workhub.coreservice.repository.TaskRepository;
import com.gnl.workhub.coreservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskCommentService {
    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public TaskCommentDto.Response addComment(UUID taskId, TaskCommentDto.Request request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User author = securityUtil.getCurrentUser();

        TaskComment comment = TaskComment.builder()
                .content(request.content())
                .task(task)
                .author(author)
                .build();

        TaskComment saved = commentRepository.save(comment);

        return new TaskCommentDto.Response(
                saved.getId(),
                saved.getContent(),
                author.getFullName(),
                author.getId(),
                saved.getCreatedAt()
        );
    }

}