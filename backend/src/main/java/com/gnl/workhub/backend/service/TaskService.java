package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.dto.*;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.ProjectMember;
import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.enums.TaskPriority;
import com.gnl.workhub.backend.enums.TaskStatus;
import com.gnl.workhub.backend.enums.UserRole;
import com.gnl.workhub.backend.exception.ResourceNotFoundException;
import com.gnl.workhub.backend.mapper.TaskDetailsMapper;
import com.gnl.workhub.backend.mapper.TaskMapper;
import com.gnl.workhub.backend.repository.ProjectMemberRepository;
import com.gnl.workhub.backend.repository.ProjectRepository;
import com.gnl.workhub.backend.repository.TaskRepository;
import com.gnl.workhub.backend.repository.UserRepository;
import com.gnl.workhub.backend.specification.TaskSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TaskDetailsMapper taskDetailsMapper;
    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public TaskResponse createTask(UUID projectId, TaskRequest request) {
        User currentUser = getCurrentUser();

        // 1. Fetch Project based on URL ID
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        // 2. SECURITY: Does the creator have access to this project?
        validateProjectAccess(project, currentUser);

        // 3. Validate Assignee (if provided)
        User assignee = null;
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getAssignedToId()));
            validateAssigneeMembership(project, assignee);
        }

        // 4. Save
        Task task = taskMapper.toEntity(request, project, assignee, currentUser);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(UUID projectId, UUID taskId, UpdateTaskRequest request) {
        Task task = getValidatedTask(projectId, taskId);
        User currentUser = getCurrentUser();

        // SECURITY: Can the user edit tasks in this project?
        validateTaskAccess(task, currentUser);

        User assignee = null;
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            validateAssigneeMembership(task.getProject(), assignee);
        }

        taskMapper.updateEntityFromRequest(request, task, assignee);
        Task savedTask = taskRepository.save(task);

        // Activity log
        Task oldState = task.toBuilder().build();
        activityLogService.logTaskUpdate(oldState, savedTask, currentUser);

        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public void deleteTask(UUID projectId, UUID taskId) {
        Task task = getValidatedTask(projectId, taskId);
        User currentUser = getCurrentUser();

        // SECURITY: Based on your documentation, only Owner/ADMIN can delete
        if (!currentUser.getGlobalRole().equals(UserRole.ADMIN) &&
                !task.getProject().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the project owner can delete tasks.");
        }

        Task oldState = task.toBuilder().build();
        task.setDeleted(true);
        Task savedTask = taskRepository.save(task);

        activityLogService.logTaskUpdate(oldState, savedTask, currentUser);
    }

    public TaskDetailsResponse getTaskById(UUID projectId, UUID taskId) {
        Task task = getValidatedTask(projectId, taskId);
        validateTaskAccess(task, getCurrentUser());
        return taskDetailsMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByProjectId(UUID projectId, TaskFilterRequest filters, Pageable pageable) {

        // 1. Verify project existence and current user access
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateProjectAccess(project, getCurrentUser());

        // 2. Fetch with filters AND pagination
//        Page<Task> tasks = taskRepository.findAdvancedFilteredTasks(
//                projectId, status, priority, assigneeId, searchTerm, startDate, endDate, pageable
//        );
        Specification<Task> spec = TaskSpecifications.build(projectId, filters);
        Page<Task> tasks = taskRepository.findAll(spec, pageable);

        // 3. Map the Page of entities to a Page of Responses
        return tasks.map(taskMapper::toResponse);
    }

    // --- NEW HELPER FOR THE "SNEAKY USER" TEST ---
    private Task getValidatedTask(UUID projectId, UUID taskId) {
//        Task task = taskRepository.findById(taskId)
//                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        Task task = taskRepository.findByIdAndDeletedFalse(taskId)
                .filter(eachtask -> eachtask.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found or has been deleted"));

        // The "Sneaky User" check: Ensure task belongs to the project in the URL
        if (!task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Task ID does not belong to the specified Project ID.");
        }
        return task;
    }

    private void validateProjectAccess(Project project, User user) {
        if (user.getGlobalRole().equals(UserRole.ADMIN)) return;

        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = projectMemberRepository.existsById(
                new ProjectMember.ProjectMemberId(project.getId(), user.getId())
        );

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Access Denied: You are not a member of this project.");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private void validateTaskAccess(Task task, User user) {
        if (user.getGlobalRole().equals(UserRole.ADMIN)) return;

        boolean isOwner = task.getProject().getOwner().getId().equals(user.getId());
        boolean isMember = projectMemberRepository.existsById(
                new ProjectMember.ProjectMemberId(task.getProject().getId(), user.getId())
        );

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("You do not have access to tasks in this project.");
        }
    }

    private void validateAssigneeMembership(Project project, User assignee) {
        if (assignee == null) return;

        boolean isMember = projectMemberRepository.existsById(
                new ProjectMember.ProjectMemberId(project.getId(), assignee.getId())
        );
        boolean isOwner = project.getOwner().getId().equals(assignee.getId());

        if (!isMember && !isOwner) {
            throw new IllegalArgumentException("Assignee must be a member of the project.");
        }
    }
}