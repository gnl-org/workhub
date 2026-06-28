package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.dto.*;
import com.gnl.workhub.backend.entity.*;
import com.gnl.workhub.backend.enums.SprintStatus;
import com.gnl.workhub.backend.enums.TaskStatus;
import com.gnl.workhub.backend.enums.UserRole;
import com.gnl.workhub.backend.exception.ResourceNotFoundException;
import com.gnl.workhub.backend.mapper.SprintMapper;
import com.gnl.workhub.backend.mapper.TaskMapper;
import com.gnl.workhub.backend.repository.*;
import com.gnl.workhub.backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {

    private static final Set<TaskStatus> COMPLETE_STATUSES = Set.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED);

    private final SprintRepository sprintRepository;
    private final SprintMapper sprintMapper;
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;
    private final WorkStageService workStageService;
    private final WorkStageRepository workStageRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final SecurityUtil securityUtil;

    public List<SprintResponse> listSprints(UUID projectId) {
        validateProjectAccess(projectId);
        return sprintRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponseWithCounts)
                .toList();
    }

    public SprintResponse getActiveSprint(UUID projectId) {
        validateProjectAccess(projectId);
        Sprint sprint = sprintRepository.findOneByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active sprint found"));
        return toResponseWithCounts(sprint);
    }

    public List<SprintResponse> listClosedSprints(UUID projectId) {
        validateProjectAccess(projectId);
        return sprintRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, SprintStatus.CLOSED).stream()
                .map(this::toResponseWithCounts)
                .toList();
    }

    public SprintDetailResponse getSprintDetail(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        validateProjectAccess(sprint.getProject().getId());

        SprintDetailResponse response = new SprintDetailResponse();
        response.setSprint(toResponseWithCounts(sprint));

        List<TaskResponse> taskResponses = taskRepository.findBySprintIdAndDeletedFalse(sprintId)
                .stream()
                .map(taskMapper::toResponse)
                .toList();
        response.setTasks(taskResponses);

        return response;
    }

    @Transactional
    public SprintResponse createSprint(UUID projectId, CreateSprintRequest request) {
        validateProjectAccess(projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        String name = request.getName() != null ? request.getName() : nextSprintName(projectId);

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName(name);
        sprint.setGoal(request.getGoal());
        sprint.setStatus(SprintStatus.PLANNED);
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint = sprintRepository.save(sprint);

        workStageService.createSprintStage(project, sprint);

        return toResponseWithCounts(sprint);
    }

    @Transactional
    public SprintResponse updateSprint(UUID sprintId, UpdateSprintRequest request) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        validateProjectAccess(sprint.getProject().getId());

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new IllegalArgumentException("Only planned sprints can be edited");
        }

        if (request.getName() != null) sprint.setName(request.getName());
        if (request.getGoal() != null) sprint.setGoal(request.getGoal());
        if (request.getStartDate() != null) sprint.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) sprint.setEndDate(request.getEndDate());

        return toResponseWithCounts(sprintRepository.save(sprint));
    }

    @Transactional
    public SprintResponse startSprint(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        UUID projectId = sprint.getProject().getId();
        validateProjectAccess(projectId);

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new IllegalArgumentException("Only planned sprints can be started");
        }

        if (sprintRepository.existsByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)) {
            throw new IllegalStateException("Another sprint is already active. Close it first.");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        if (sprint.getStartDate() == null) {
            sprint.setStartDate(LocalDate.now());
        }

        sprint = sprintRepository.save(sprint);
        activityLogService.logProjectEvent(sprint.getProject(), securityUtil.getCurrentUser(), "SPRINT_STARTED", sprint.getName());

        return toResponseWithCounts(sprint);
    }

    @Transactional
    public CloseSprintResponse closeSprint(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        UUID projectId = sprint.getProject().getId();
        validateProjectAccess(projectId);

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active sprints can be closed");
        }

        sprint.setStatus(SprintStatus.CLOSED);
        sprint.setClosedAt(Instant.now());
        if (sprint.getEndDate() == null) {
            sprint.setEndDate(LocalDate.now());
        }
        sprintRepository.save(sprint);

        List<Task> incompleteTasks = taskRepository.findBySprintIdAndDeletedFalse(sprintId)
                .stream()
                .filter(t -> !COMPLETE_STATUSES.contains(t.getStatus()))
                .toList();

        CloseSprintResponse response = new CloseSprintResponse();
        response.setTotalIncompleteTasks(incompleteTasks.size());

        if (!incompleteTasks.isEmpty()) {
            Sprint targetSprint = findTargetPlannedSprint(projectId);
            if (targetSprint == null) {
                CreateSprintRequest autoReq = new CreateSprintRequest();
                autoReq.setName(nextSprintName(projectId));
                targetSprint = sprintRepository.save(createSprintEntity(projectId, autoReq));
                workStageService.createSprintStage(sprint.getProject(), targetSprint);
            }

            WorkStage targetStage = workStageRepository.findBySprintId(targetSprint.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Work stage not found for target sprint"));

            int maxSort = taskRepository.findMaxSortOrderByWorkStageId(targetStage.getId())
                    .orElse(-1);

            for (int i = 0; i < incompleteTasks.size(); i++) {
                Task task = incompleteTasks.get(i);
                task.setSprint(targetSprint);
                task.setWorkStage(targetStage);
                task.setSortOrder(maxSort + 1 + i);
            }
            taskRepository.saveAll(incompleteTasks);

            response.setMovedToSprintTasks(incompleteTasks.size());
            response.setTargetSprintName(targetSprint.getName());
        }

        activityLogService.logProjectEvent(sprint.getProject(), securityUtil.getCurrentUser(), "SPRINT_CLOSED", sprint.getName());

        return response;
    }

    @Transactional
    public void assignTasks(UUID sprintId, AssignTasksRequest request) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        UUID projectId = sprint.getProject().getId();
        validateProjectAccess(projectId);

        WorkStage sprintStage = workStageRepository.findBySprintId(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Work stage not found for sprint"));

        int maxSort = taskRepository.findMaxSortOrderByWorkStageId(sprintStage.getId())
                .orElse(-1);

        List<Task> tasks = taskRepository.findAllById(request.getTaskIds());
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (!task.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Task " + task.getId() + " does not belong to this project");
            }
            task.setSprint(sprint);
            task.setWorkStage(sprintStage);
            task.setSortOrder(maxSort + 1 + i);
        }

        taskRepository.saveAll(tasks);
    }

    @Transactional
    public void removeTasks(UUID sprintId, AssignTasksRequest request) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        validateProjectAccess(sprint.getProject().getId());

        List<Task> tasks = taskRepository.findAllById(request.getTaskIds());
        for (Task task : tasks) {
            task.setSprint(null);
        }

        taskRepository.saveAll(tasks);
    }

    private SprintResponse toResponseWithCounts(Sprint sprint) {
        SprintResponse response = sprintMapper.toResponse(sprint);
        List<Task> sprintTasks = taskRepository.findBySprintIdAndDeletedFalse(sprint.getId());
        response.setTotalTasks(sprintTasks.size());
        response.setCompletedTasks((int) sprintTasks.stream()
                .filter(t -> TaskStatus.COMPLETED == t.getStatus() || TaskStatus.CANCELLED == t.getStatus())
                .count());
        response.setIncompleteTasks(response.getTotalTasks() - response.getCompletedTasks());
        return response;
    }

    private Sprint createSprintEntity(UUID projectId, CreateSprintRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName(request.getName() != null ? request.getName() : nextSprintName(projectId));
        sprint.setGoal(request.getGoal());
        sprint.setStatus(SprintStatus.PLANNED);
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        return sprint;
    }

    private Sprint findTargetPlannedSprint(UUID projectId) {
        return sprintRepository.findByProjectIdAndStatus(projectId, SprintStatus.PLANNED).stream()
                .min(Comparator
                        .comparingInt((Sprint s) -> parseSprintNumber(s.getName()).orElse(Integer.MAX_VALUE))
                        .thenComparing(Sprint::getCreatedAt))
                .orElse(null);
    }

    public String nextSprintName(UUID projectId) {
        int max = sprintRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(Sprint::getName)
                .mapToInt(n -> parseSprintNumber(n).orElse(0))
                .max()
                .orElse(0);
        return "Sprint " + (max + 1);
    }

    OptionalInt parseSprintNumber(String name) {
        Matcher m = Pattern.compile("(\\d+)").matcher(name);
        if (m.find()) return OptionalInt.of(Integer.parseInt(m.group(1)));
        return OptionalInt.empty();
    }

    private void validateProjectAccess(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User user = securityUtil.getCurrentUser();
        if (user.getGlobalRole().equals(UserRole.ADMIN)) return;

        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = projectMemberRepository.existsById(
                new ProjectMember.ProjectMemberId(project.getId(), user.getId())
        );

        if (!isOwner && !isMember) {
            throw new AccessDeniedException("Access Denied: You are not a member of this project.");
        }
    }

}
