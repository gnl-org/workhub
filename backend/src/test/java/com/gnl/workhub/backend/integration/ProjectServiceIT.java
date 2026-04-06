package com.gnl.workhub.backend.integration;

import com.gnl.workhub.backend.dto.ProjectRequest;
import com.gnl.workhub.backend.dto.ProjectResponse;
import com.gnl.workhub.backend.dto.UpdateProjectRequest;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.enums.ProjectStatus;
import com.gnl.workhub.backend.enums.UserRole;
import com.gnl.workhub.backend.repository.ProjectRepository;
import com.gnl.workhub.backend.repository.UserRepository;
import com.gnl.workhub.backend.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProjectServiceIT extends BaseIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User testOwner;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        // Create a real user in the test database
        testOwner = new User();
        testOwner.setEmail("owner@workhub.com");
        testOwner.setPasswordHash("hashed_password");
        testOwner.setFullName("Project Owner");
        testOwner.setGlobalRole(UserRole.USER);

        User savedOwner = userRepository.save(testOwner);
        ownerId = savedOwner.getId();
    }

    @Test
    void shouldCreateProjectInRealDatabase() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setTitle("Real Integration Project");
        request.setDescription("Testing with real database");
        request.setOwnerId(ownerId);

        // Act
        ProjectResponse response = projectService.createProject(request);

        // Assert
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Real Integration Project");
        assertThat(response.getDescription()).isEqualTo("Testing with real database");
        assertThat(response.getOwnerName()).isEqualTo("Project Owner");

        // Verify it persists in the database
        Project savedProject = projectRepository.findById(response.getId()).orElse(null);
        assertThat(savedProject).isNotNull();
        assertThat(savedProject.getTitle()).isEqualTo("Real Integration Project");
        assertThat(savedProject.getOwner().getId()).isEqualTo(ownerId);
    }

    @Test
    void shouldRetrieveProjectsByOwnerId() {
        // Arrange
        ProjectRequest request1 = new ProjectRequest();
        request1.setTitle("Project One");
        request1.setOwnerId(ownerId);

        ProjectRequest request2 = new ProjectRequest();
        request2.setTitle("Project Two");
        request2.setOwnerId(ownerId);

        projectService.createProject(request1);
        projectService.createProject(request2);

        // Act
        List<ProjectResponse> projects = projectService.getProjectsByOwnerId(ownerId);

        // Assert
        assertThat(projects).hasSize(2);
        assertThat(projects).extracting("title").containsExactlyInAnyOrder("Project One", "Project Two");
        assertThat(projects).allMatch(p -> p.getOwnerName().equals("Project Owner"));
    }

    @Test
    void shouldUpdateProjectStatus() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setTitle("Project to Archive");
        request.setDescription("Original description");
        request.setOwnerId(ownerId);

        ProjectResponse created = projectService.createProject(request);
        UUID projectId = created.getId();

        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setStatus(ProjectStatus.ARCHIVED);
        updateRequest.setTitle("Updated Title");

        // Act
        ProjectResponse updated = projectService.updateProject(projectId, updateRequest);

        // Assert
        assertThat(updated.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getDescription()).isEqualTo("Original description"); // Should remain unchanged

        // Verify persistence
        Project persisted = projectRepository.findById(projectId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
    }

    @Test
    void shouldDeleteProjectCascade() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setTitle("Project to Delete");
        request.setOwnerId(ownerId);

        ProjectResponse created = projectService.createProject(request);
        UUID projectId = created.getId();

        assertThat(projectRepository.findById(projectId)).isPresent();

        // Act
        projectService.deleteProject(projectId);

        // Assert
        assertThat(projectRepository.findById(projectId)).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenCreatingProjectWithNonexistentOwner() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setTitle("Invalid Project");
        request.setOwnerId(UUID.randomUUID()); // Non-existent user

        // Act & Assert
        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Owner not found");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentProject() {
        // Arrange
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle("Updated");

        // Act & Assert
        assertThatThrownBy(() -> projectService.updateProject(UUID.randomUUID(), updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void shouldPartialUpdateProject() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setTitle("Original Title");
        request.setDescription("Original Description");
        request.setOwnerId(ownerId);

        ProjectResponse created = projectService.createProject(request);
        UUID projectId = created.getId();

        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle("New Title");
        // Description and status are null, should remain unchanged

        // Act
        ProjectResponse updated = projectService.updateProject(projectId, updateRequest);

        // Assert
        assertThat(updated.getTitle()).isEqualTo("New Title");
        assertThat(updated.getDescription()).isEqualTo("Original Description");
    }
}