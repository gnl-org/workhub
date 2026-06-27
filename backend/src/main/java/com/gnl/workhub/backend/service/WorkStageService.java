package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.dto.CreateWorkStageRequest;
import com.gnl.workhub.backend.dto.UpdateWorkStageRequest;
import com.gnl.workhub.backend.dto.WorkStageResponse;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.ProjectMember;
import com.gnl.workhub.backend.entity.Sprint;
import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.entity.WorkStage;
import com.gnl.workhub.backend.enums.SprintStatus;
import com.gnl.workhub.backend.enums.UserRole;
import com.gnl.workhub.backend.exception.ResourceNotFoundException;
import com.gnl.workhub.backend.mapper.WorkStageMapper;
import com.gnl.workhub.backend.repository.ProjectRepository;
import com.gnl.workhub.backend.repository.ProjectMemberRepository;
import com.gnl.workhub.backend.repository.TaskRepository;
import com.gnl.workhub.backend.repository.UserRepository;
import com.gnl.workhub.backend.repository.WorkStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkStageService {

    private static final List<String> SEEDED_STAGE_NAMES = List.of("Backlog", "Ready for Refinement", "Ready for Sprint");

    private final WorkStageRepository workStageRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkStageMapper workStageMapper;

    public List<WorkStageResponse> listVisibleStages(UUID projectId) {
        validateProjectAccess(projectId);
        return workStageRepository.findByProjectIdOrderBySortOrderAsc(projectId).stream()
                .filter(this::isStageVisible)
                .map(workStageMapper::toResponse)
                .toList();
    }

    public List<WorkStage> listAllStages(UUID projectId) {
        return workStageRepository.findByProjectIdOrderBySortOrderAsc(projectId);
    }

    @Transactional
    public WorkStageResponse createStage(UUID projectId, CreateWorkStageRequest request) {
        validateProjectAccess(projectId);

        if (workStageRepository.existsByProjectIdAndName(projectId, request.getName())) {
            throw new IllegalArgumentException("A stage with this name already exists");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        int maxSortOrder = workStageRepository.findByProjectIdOrderBySortOrderAsc(projectId).stream()
                .mapToInt(WorkStage::getSortOrder)
                .max()
                .orElse(-1);

        WorkStage stage = new WorkStage();
        stage.setProject(project);
        stage.setName(request.getName());
        stage.setSortOrder(maxSortOrder + 1);

        return workStageMapper.toResponse(workStageRepository.save(stage));
    }

    @Transactional
    public WorkStageResponse renameStage(UUID projectId, UUID stageId, UpdateWorkStageRequest request) {
        validateProjectAccess(projectId);
        WorkStage stage = getValidatedStage(projectId, stageId);

        if (workStageRepository.existsByProjectIdAndName(projectId, request.getName())
                && !stage.getName().equals(request.getName())) {
            throw new IllegalArgumentException("A stage with this name already exists");
        }

        stage.setName(request.getName());
        return workStageMapper.toResponse(workStageRepository.save(stage));
    }

    @Transactional
    public void deleteStage(UUID projectId, UUID stageId) {
        validateProjectAccess(projectId);
        WorkStage stage = getValidatedStage(projectId, stageId);

        if (SEEDED_STAGE_NAMES.contains(stage.getName()) && stage.getSprint() == null) {
            throw new IllegalArgumentException("Cannot delete default stages");
        }

        WorkStage backlogStage = workStageRepository.findDefaultBacklogStage(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Default Backlog stage not found"));

        List<Task> tasks = taskRepository.findByWorkStageIdAndDeletedFalseOrderBySortOrderAsc(stageId);
        for (Task task : tasks) {
            task.setWorkStage(backlogStage);
            taskRepository.save(task);
        }

        workStageRepository.delete(stage);
    }

    @Transactional
    public void reorderStages(UUID projectId, List<UUID> stageIds) {
        validateProjectAccess(projectId);
        List<WorkStage> stages = workStageRepository.findByProjectIdOrderBySortOrderAsc(projectId);

        for (int i = 0; i < stageIds.size(); i++) {
            UUID id = stageIds.get(i);
            int order = i;
            stages.stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .ifPresent(s -> s.setSortOrder(order));
        }

        workStageRepository.saveAll(stages);
    }

    @Transactional
    public void seedDefaultStages(Project project) {
        for (int i = 0; i < SEEDED_STAGE_NAMES.size(); i++) {
            if (!workStageRepository.existsByProjectIdAndName(project.getId(), SEEDED_STAGE_NAMES.get(i))) {
                WorkStage stage = new WorkStage();
                stage.setProject(project);
                stage.setName(SEEDED_STAGE_NAMES.get(i));
                stage.setSortOrder(i);
                workStageRepository.save(stage);
            }
        }
    }

    @Transactional
    public WorkStage createSprintStage(Project project, Sprint sprint) {
        WorkStage stage = new WorkStage();
        stage.setProject(project);
        stage.setSprint(sprint);
        stage.setName(sprint.getName());
        int maxSort = workStageRepository.findByProjectIdOrderBySortOrderAsc(project.getId()).stream()
                .mapToInt(WorkStage::getSortOrder)
                .max()
                .orElse(-1);
        stage.setSortOrder(maxSort + 1);
        return workStageRepository.save(stage);
    }

    public WorkStage getDefaultBacklogStage(UUID projectId) {
        return workStageRepository.findDefaultBacklogStage(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Default Backlog stage not found"));
    }

    boolean isStageVisible(WorkStage stage) {
        if (stage.getSprint() == null) return true;
        var s = stage.getSprint().getStatus();
        return s == SprintStatus.PLANNED || s == SprintStatus.ACTIVE;
    }

    private WorkStage getValidatedStage(UUID projectId, UUID stageId) {
        WorkStage stage = workStageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Work stage not found"));

        if (!stage.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Work stage does not belong to this project");
        }

        return stage;
    }

    private void validateProjectAccess(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User user = getCurrentUser();
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
}
