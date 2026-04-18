package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.dto.TaskCommentDto;
import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.entity.TaskComment;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.exception.ResourceNotFoundException;
import com.gnl.workhub.backend.repository.TaskCommentRepository;
import com.gnl.workhub.backend.repository.TaskRepository;
import com.gnl.workhub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskCommentService {
    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskCommentDto.Response addComment(UUID taskId, TaskCommentDto.Request request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User author = getCurrentUser();

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

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}