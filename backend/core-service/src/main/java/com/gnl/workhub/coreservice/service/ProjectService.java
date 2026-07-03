package com.gnl.workhub.coreservice.service;

import com.gnl.workhub.coreservice.dto.ProjectRequest;
import com.gnl.workhub.coreservice.dto.ProjectResponse;
import com.gnl.workhub.coreservice.dto.ProjectStats;
import com.gnl.workhub.coreservice.dto.UpdateProjectRequest;
import com.gnl.workhub.coreservice.entity.Project;
import com.gnl.workhub.coreservice.entity.ProjectMember;
import com.gnl.workhub.coreservice.entity.User;
import com.gnl.workhub.coreservice.enums.ProjectRole;
import com.gnl.workhub.coreservice.enums.TaskStatus;
import com.gnl.workhub.coreservice.enums.UserRole;
import com.gnl.workhub.coreservice.exception.ResourceNotFoundException;
import com.gnl.workhub.coreservice.mapper.ProjectMapper;
import com.gnl.workhub.coreservice.repository.ProjectMemberRepository;
import com.gnl.workhub.coreservice.repository.ProjectRepository;
import com.gnl.workhub.coreservice.repository.TaskRepository;
import com.gnl.workhub.coreservice.repository.UserRepository;
import com.gnl.workhub.coreservice.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMapper projectMapper;
    private final TaskRepository taskRepository;
    private final WorkStageService workStageService;
    private final SecurityUtil securityUtil;

    // --- HELPER METHODS ---
    private List<ProjectResponse> toResponseList(List<Project> projects) {
        return projects.stream()
                .map(projectMapper::toResponse)
                .toList(); // Cleaner Java 16+ syntax
    }

    // --- SERVICE METHODS ---

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(
            value = "projects",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()",
            sync = true
    )
    public List<ProjectResponse> getAllProjects() {
//        return toResponseList(projectRepository.findAll());
        return toResponseList(projectRepository.findAllWithOwners());
    }

    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));

        validateProjectAccess(project, securityUtil.getCurrentUser());
        return projectMapper.toResponse(project);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse createProject(ProjectRequest request) {
        User currentUser = securityUtil.getCurrentUser();

        Project project = projectMapper.toEntity(request, currentUser);
        Project savedProject = projectRepository.save(project);

        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(savedProject);
        ownerMember.setUser(currentUser);
        ownerMember.setProjectRole(ProjectRole.OWNER); // Or your specific Enum value

        projectMemberRepository.save(ownerMember);

        workStageService.seedDefaultStages(savedProject);

        return projectMapper.toResponse(savedProject);
    }

    public List<ProjectResponse> getMyProjects() {
        return toResponseList(projectRepository.findByOwnerId(securityUtil.getCurrentUser().getId()));
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        validateProjectAccess(project, securityUtil.getCurrentUser());

        projectMapper.updateEntityFromRequest(request, project);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        User user = securityUtil.getCurrentUser();
        if (!project.getOwner().getId().equals(user.getId()) && user.getGlobalRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only the owner can delete this project");
        }

        projectRepository.delete(project);
    }

    public ProjectStats getProjectStats(UUID projectId) {
        // 1. Verify access...
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        User user = securityUtil.getCurrentUser();
        if (!project.getOwner().getId().equals(user.getId()) && user.getGlobalRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only the owner can delete this project");
        }

        // 2. Gather data
        long total = taskRepository.countByProjectId(projectId);
        long overdue = taskRepository.countOverdueTasks(projectId);

        // Initialize with zeros for every status
        Map<TaskStatus, Long> statusMap = new EnumMap<>(TaskStatus.class);
        Arrays.stream(TaskStatus.values()).forEach(s -> statusMap.put(s, 0L));

        // Merge the database results into the map
        taskRepository.countTasksByStatus(projectId).forEach(row ->
                statusMap.put((TaskStatus) row[0], (Long) row[1])
        );

        return ProjectStats.builder()
                .totalTasks(total)
                .statusCounts(statusMap)
                .overdueTasks(overdue)
                .build();
    }

    private void validateProjectAccess(Project project, User user) {
        // 1. Check if Admin
        boolean isAdmin = user.getGlobalRole().equals(UserRole.ADMIN);
        if (isAdmin) return; // Admins get a pass

        // 2. Check if Owner
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        if (isOwner) return;

        // 3. Check if Member
        boolean isMember = projectMemberRepository.existsById(
                new ProjectMember.ProjectMemberId(project.getId(), user.getId())
        );

        if (!isMember) {
            throw new AccessDeniedException("Access Denied: You are not a member of this project.");
        }
    }
}
