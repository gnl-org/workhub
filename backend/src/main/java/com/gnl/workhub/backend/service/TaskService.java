package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.dto.TaskRequest;
import com.gnl.workhub.backend.dto.TaskResponse;
import com.gnl.workhub.backend.dto.UpdateTaskRequest;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.exception.ResourceNotFoundException;
import com.gnl.workhub.backend.mapper.TaskMapper;
import com.gnl.workhub.backend.repository.ProjectRepository;
import com.gnl.workhub.backend.repository.TaskRepository;
import com.gnl.workhub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));

        // 2. Validate Assignee (Optional)
        User assignee = null;
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getAssignedToId()));
        }

        // 3. Map DTO to Entity
        Task task = taskMapper.toEntity(request, project, assignee);

        // 4. Save to Postgres
        Task savedTask = taskRepository.save(task);

        // 5. Map back to Response DTO
        return taskMapper.toResponse(savedTask);
    }

    public List<TaskResponse> getTasksByProjectId(UUID projectId) {
        // Verify project exists
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByProjectIdAndUserId(UUID projectId, UUID userId) {
        // Verify project and user exist
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<Task> tasks = taskRepository.findByProjectIdAndAssignedToId(projectId, userId);
        return tasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        // Validate and fetch assignee if provided
        User assignee = null;
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getAssignedToId()));
        }

        // Update only non-null fields
        taskMapper.updateEntityFromRequest(request, task, assignee);

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        taskRepository.delete(task);
    }

    public TaskResponse getTaskById(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
        return taskMapper.toResponse(task);
    }
}