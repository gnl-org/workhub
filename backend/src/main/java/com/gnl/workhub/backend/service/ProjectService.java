package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.dto.ProjectRequest;
import com.gnl.workhub.backend.dto.ProjectResponse;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.ProjectMember;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.enums.ProjectRole;
import com.gnl.workhub.backend.enums.ProjectStatus;
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
                .orElseThrow(() -> new RuntimeException("Project not found")); // TODO: throw custom exception to handle globally
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        // 2. Conversion
        Project project = projectMapper.toEntity(request, owner);

        // 3. Persistence
        Project savedProject = projectRepository.save(project);

        // 4. Return formatted data
        return projectMapper.toResponse(savedProject);
    }
}
