package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.dto.TaskRequest;
import com.gnl.workhub.backend.dto.TaskResponse;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.mapper.TaskMapper;
import com.gnl.workhub.backend.repository.ProjectRepository;
import com.gnl.workhub.backend.repository.TaskRepository;
import com.gnl.workhub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        // 1. Validate Project (Mandatory)
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + request.getProjectId()));

        // 2. Validate Assignee (Optional)
        User assignee = null;
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getAssignedToId()));
        }

        // 3. Map DTO to Entity
        Task task = taskMapper.toEntity(request, project, assignee);

        // 4. Save to Postgres
        Task savedTask = taskRepository.save(task);

        // 5. Map back to Response DTO
        return taskMapper.toResponse(savedTask);
    }
}