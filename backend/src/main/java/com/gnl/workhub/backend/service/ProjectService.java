package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.dto.ProjectRequest;
import com.gnl.workhub.backend.dto.ProjectResponse;
import com.gnl.workhub.backend.dto.UpdateProjectRequest;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.ProjectMember;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.enums.ProjectRole;
import com.gnl.workhub.backend.enums.ProjectStatus;
import com.gnl.workhub.backend.exception.ResourceNotFoundException;
import com.gnl.workhub.backend.mapper.ProjectMapper;
import com.gnl.workhub.backend.repository.ProjectMemberRepository;
import com.gnl.workhub.backend.repository.ProjectRepository;
import com.gnl.workhub.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMapper projectMapper;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found" + id));
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        // 2. Conversion
        Project project = projectMapper.toEntity(request, owner);

        // 3. Persistence
        Project savedProject = projectRepository.save(project);

        // 4. Return formatted data
        return projectMapper.toResponse(savedProject);
    }

    public List<ProjectResponse> getProjectsByOwnerId(UUID ownerId) {
        // Verify user exists
        userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + ownerId));

        List<Project> projects = projectRepository.findByOwnerId(ownerId);
        return projects.stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getProjectsByUserMembership(UUID userId) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Get all projects where user is a member
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(ProjectMember::getProject)
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        // Update only non-null fields
        projectMapper.updateEntityFromRequest(request, project);

        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        projectRepository.delete(project);
    }
}
