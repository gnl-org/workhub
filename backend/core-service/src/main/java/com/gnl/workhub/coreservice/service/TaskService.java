package com.gnl.workhub.coreservice.service;

import com.gnl.workhub.coreservice.dto.*;
import com.gnl.workhub.coreservice.entity.*;
import com.gnl.workhub.coreservice.enums.NotificationType;
import com.gnl.workhub.coreservice.enums.SprintStatus;
import com.gnl.workhub.coreservice.enums.TaskPriority;
import com.gnl.workhub.coreservice.enums.TaskStatus;
import com.gnl.workhub.coreservice.enums.UserRole;
import com.gnl.workhub.coreservice.exception.ResourceNotFoundException;
import com.gnl.workhub.coreservice.mapper.TaskDetailsMapper;
import com.gnl.workhub.coreservice.mapper.TaskMapper;
import com.gnl.workhub.coreservice.repository.*;
import com.gnl.workhub.coreservice.specification.TaskSpecifications;
import com.gnl.workhub.coreservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final WorkStageService workStageService;
    private final WorkStageRepository workStageRepository;
    private final SecurityUtil securityUtil;
    private final NotificationProducer notificationProducer;

    @Transactional
    public TaskResponse createTask(UUID projectId, TaskRequest request) {
        User currentUser = securityUtil.getCurrentUser();

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

        // 4. Set default work stage if not specified
        Task task = taskMapper.toEntity(request, project, assignee, currentUser);
        if (task.getWorkStage() == null) {
            WorkStage backlogStage = workStageService.getDefaultBacklogStage(projectId);
            task.setWorkStage(backlogStage);
        }

        // 5. Set sort_order to max+1 in the stage
        int maxSort = taskRepository.findMaxSortOrderByWorkStageId(task.getWorkStage().getId())
                .orElse(-1);
        task.setSortOrder(maxSort + 1);

        // 6. Propagate sprint from work stage (if the stage belongs to a sprint)
        if (task.getWorkStage() != null) {
            WorkStage resolvedStage = workStageRepository.findById(task.getWorkStage().getId()).orElse(null);
            if (resolvedStage != null && resolvedStage.getSprint() != null) {
                task.setSprint(resolvedStage.getSprint());
            }
        }

        TaskResponse response = taskMapper.toResponse(taskRepository.save(task));

        if (assignee != null) {
            notificationProducer.send(NotificationMessage.builder()
                    .type(NotificationType.TASK_ASSIGNED)
                    .taskId(task.getId())
                    .projectId(projectId)
                    .recipientId(assignee.getId())
                    .recipientEmail(assignee.getEmail())
                    .triggeredByUserId(currentUser.getId())
                    .message("You have been assigned to task: " + task.getTitle())
                    .build());
        }

        return response;
    }

    @Transactional
    public TaskResponse updateTask(UUID projectId, UUID taskId, UpdateTaskRequest request) {
        Task task = getValidatedTask(projectId, taskId);
        User currentUser = securityUtil.getCurrentUser();

        // SECURITY: Can the user edit tasks in this project?
        validateTaskAccess(task, currentUser);

        User assignee = null;
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            validateAssigneeMembership(task.getProject(), assignee);
        }

        User oldAssignee = task.getAssignedTo();
        taskMapper.updateEntityFromRequest(request, task, assignee);
        Task savedTask = taskRepository.save(task);

        // Activity log
        Task oldState = task.toBuilder().build();
        activityLogService.logTaskUpdate(oldState, savedTask, currentUser);

        // Notify new assignee if changed
        if (assignee != null && (oldAssignee == null || !oldAssignee.getId().equals(assignee.getId()))) {
            notificationProducer.send(NotificationMessage.builder()
                    .type(NotificationType.TASK_ASSIGNED)
                    .taskId(savedTask.getId())
                    .projectId(projectId)
                    .recipientId(assignee.getId())
                    .recipientEmail(assignee.getEmail())
                    .triggeredByUserId(currentUser.getId())
                    .message("You have been assigned to task: " + savedTask.getTitle())
                    .build());
        }

        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public void deleteTask(UUID projectId, UUID taskId) {
        Task task = getValidatedTask(projectId, taskId);
        User currentUser = securityUtil.getCurrentUser();

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
        validateTaskAccess(task, securityUtil.getCurrentUser());
        return taskDetailsMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByProjectId(UUID projectId, TaskFilterRequest filters, Pageable pageable) {

        // 1. Verify project existence and current user access
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateProjectAccess(project, securityUtil.getCurrentUser());

        // 2. Fetch with filters AND pagination
//        Page<Task> tasks = taskRepository.findAdvancedFilteredTasks(
//                projectId, status, priority, assigneeId, searchTerm, startDate, endDate, pageable
//        );
        Specification<Task> spec = TaskSpecifications.build(projectId, filters);
        Page<Task> tasks = taskRepository.findAll(spec, pageable);

        // 3. Map the Page of entities to a Page of Responses
        return tasks.map(taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BacklogResponse getBacklog(UUID projectId) {
        validateProjectAccess(projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found")), securityUtil.getCurrentUser());

        List<WorkStage> stages = workStageRepository.findByProjectIdOrderBySortOrderAsc(projectId);
        BacklogResponse response = new BacklogResponse();
        List<BacklogStageDto> stageDtos = new ArrayList<>();

        for (WorkStage stage : stages) {
            if (!workStageService.isStageVisible(stage)) continue;

            BacklogStageDto stageDto = new BacklogStageDto();
            stageDto.setId(stage.getId());
            stageDto.setName(stage.getName());
            stageDto.setSortOrder(stage.getSortOrder());

            if (stage.getSprint() != null) {
                stageDto.setSprintId(stage.getSprint().getId());
                stageDto.setSprintStatus(stage.getSprint().getStatus());
            }

            List<Task> tasks = taskRepository.findByWorkStageIdAndDeletedFalseOrderBySortOrderAsc(stage.getId());
            // Filter out tasks on closed sprints
            List<TaskResponse> taskResponses = tasks.stream()
                    .filter(t -> t.getSprint() == null || t.getSprint().getStatus() != SprintStatus.CLOSED)
                    .map(taskMapper::toResponse)
                    .toList();

            stageDto.setTasks(taskResponses);
            stageDtos.add(stageDto);
        }

        response.setStages(stageDtos);
        return response;
    }

    @Transactional
    public TaskResponse moveTask(UUID projectId, UUID taskId, MoveTaskRequest request) {
        Task task = getValidatedTask(projectId, taskId);
        User currentUser = securityUtil.getCurrentUser();
        validateTaskAccess(task, currentUser);

        WorkStage targetStage = workStageRepository.findById(request.getWorkStageId())
                .orElseThrow(() -> new ResourceNotFoundException("Work stage not found"));

        if (!targetStage.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Work stage does not belong to this project");
        }

        Task oldState = task.toBuilder().build();

        task.setWorkStage(targetStage);

        if (targetStage.getSprint() != null) {
            task.setSprint(targetStage.getSprint());
        } else {
            task.setSprint(null);
        }

        if (request.getSortOrder() != null) {
            task.setSortOrder(request.getSortOrder());
        } else {
            int maxSort = taskRepository.findMaxSortOrderByWorkStageId(targetStage.getId())
                    .orElse(-1);
            task.setSortOrder(maxSort + 1);
        }

        Task savedTask = taskRepository.save(task);
        activityLogService.logTaskUpdate(oldState, savedTask, currentUser);

        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public void reorderTasksInStage(UUID projectId, UUID stageId, ReorderTasksRequest request) {
        validateProjectAccess(projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found")), securityUtil.getCurrentUser());

        List<Task> tasks = taskRepository.findByWorkStageIdAndDeletedFalseOrderBySortOrderAsc(stageId);
        Map<UUID, Task> taskMap = tasks.stream().collect(Collectors.toMap(Task::getId, t -> t));

        for (int i = 0; i < request.getTaskIds().size(); i++) {
            Task task = taskMap.get(request.getTaskIds().get(i));
            if (task != null) {
                task.setSortOrder(i);
            }
        }

        taskRepository.saveAll(tasks);
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