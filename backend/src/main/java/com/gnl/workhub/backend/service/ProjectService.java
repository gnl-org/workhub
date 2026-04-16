package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.dto.ProjectRequest;
import com.gnl.workhub.backend.dto.ProjectResponse;
import com.gnl.workhub.backend.dto.UpdateProjectRequest;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.ProjectMember;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.enums.UserRole;
import com.gnl.workhub.backend.exception.ResourceNotFoundException;
import com.gnl.workhub.backend.mapper.ProjectMapper;
import com.gnl.workhub.backend.repository.ProjectMemberRepository;
import com.gnl.workhub.backend.repository.ProjectRepository;
import com.gnl.workhub.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMapper projectMapper;

    // --- HELPER METHODS ---
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private List<ProjectResponse> toResponseList(List<Project> projects) {
        return projects.stream()
                .map(projectMapper::toResponse)
                .toList(); // Cleaner Java 16+ syntax
    }

    // --- SERVICE METHODS ---

    @PreAuthorize("hasRole('ADMIN')")
    public List<ProjectResponse> getAllProjects() {
        return toResponseList(projectRepository.findAll());
    }

    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));

        validateProjectAccess(project, getCurrentUser());
        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        Project project = projectMapper.toEntity(request, getCurrentUser());
        return projectMapper.toResponse(projectRepository.save(project));
    }

    public List<ProjectResponse> getMyProjects() {
        return toResponseList(projectRepository.findByOwnerId(getCurrentUser().getId()));
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        validateProjectAccess(project, getCurrentUser());

        projectMapper.updateEntityFromRequest(request, project);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        User user = getCurrentUser();
        if (!project.getOwner().getId().equals(user.getId()) && user.getGlobalRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only the owner can delete this project");
        }

        projectRepository.delete(project);
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
