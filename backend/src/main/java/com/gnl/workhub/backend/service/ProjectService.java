package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.model.Project;
import com.gnl.workhub.backend.repository.ProjectRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Data
public class ProjectService {
    private final ProjectRepository projectRepository;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found")); // TODO: throw custom exception to handle globally
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }
}
