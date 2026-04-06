package com.gnl.workhub.backend.mapper;

import com.gnl.workhub.backend.dto.ProjectRequest;
import com.gnl.workhub.backend.dto.ProjectResponse;
import com.gnl.workhub.backend.dto.UpdateProjectRequest;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.enums.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProjectMapperTest {
    private ProjectMapper projectMapper;
    private User testOwner;

    @BeforeEach
    void setUp() {
        projectMapper = new ProjectMapper();

        testOwner = new User();
        testOwner.setId(UUID.randomUUID());
        testOwner.setFullName("Project Owner");
    }

    @Test
    void testToEntity_WithValidRequest() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setTitle("New Project");
        request.setDescription("Project Description");

        // Act
        Project project = projectMapper.toEntity(request, testOwner);

        // Assert
        assertEquals("New Project", project.getTitle());
        assertEquals("Project Description", project.getDescription());
        assertEquals(testOwner, project.getOwner());
    }

    @Test
    void testToEntity_WithNullDescription() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setTitle("Project without Description");
        request.setDescription(null);

        // Act
        Project project = projectMapper.toEntity(request, testOwner);

        // Assert
        assertEquals("Project without Description", project.getTitle());
        assertNull(project.getDescription());
        assertEquals(testOwner, project.getOwner());
    }

    @Test
    void testToResponse_WithValidProject() {
        // Arrange
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTitle("Test Project");
        project.setDescription("Test Description");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(testOwner);

        // Act
        ProjectResponse response = projectMapper.toResponse(project);

        // Assert
        assertEquals(project.getId(), response.getId());
        assertEquals("Test Project", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals(ProjectStatus.ACTIVE, response.getStatus());
        assertEquals("Project Owner", response.getOwnerName());
    }

    @Test
    void testToResponse_WithNullOwner() {
        // Arrange
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setTitle("Project without Owner");
        project.setDescription("Description");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(null);

        // Act
        ProjectResponse response = projectMapper.toResponse(project);

        // Assert
        assertEquals("Project without Owner", response.getTitle());
        assertNull(response.getOwnerName());
    }

    @Test
    void testUpdateEntityFromRequest_UpdateAllFields() {
        // Arrange
        Project project = new Project();
        project.setTitle("Old Title");
        project.setDescription("Old Description");
        project.setStatus(ProjectStatus.ACTIVE);

        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setTitle("New Title");
        request.setDescription("New Description");
        request.setStatus(ProjectStatus.ARCHIVED);

        // Act
        projectMapper.updateEntityFromRequest(request, project);

        // Assert
        assertEquals("New Title", project.getTitle());
        assertEquals("New Description", project.getDescription());
        assertEquals(ProjectStatus.ARCHIVED, project.getStatus());
    }

    @Test
    void testUpdateEntityFromRequest_UpdatePartialFields() {
        // Arrange
        Project project = new Project();
        project.setTitle("Original Title");
        project.setDescription("Original Description");
        project.setStatus(ProjectStatus.ACTIVE);

        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setTitle("Updated Title");
        request.setDescription(null); // Should not update
        request.setStatus(null); // Should not update

        // Act
        projectMapper.updateEntityFromRequest(request, project);

        // Assert
        assertEquals("Updated Title", project.getTitle());
        assertEquals("Original Description", project.getDescription()); // Should remain unchanged
        assertEquals(ProjectStatus.ACTIVE, project.getStatus()); // Should remain unchanged
    }

    @Test
    void testUpdateEntityFromRequest_NoUpdates() {
        // Arrange
        Project project = new Project();
        project.setTitle("Title");
        project.setDescription("Description");
        project.setStatus(ProjectStatus.ACTIVE);

        UpdateProjectRequest request = new UpdateProjectRequest();
        // All fields are null

        // Act
        projectMapper.updateEntityFromRequest(request, project);

        // Assert
        assertEquals("Title", project.getTitle());
        assertEquals("Description", project.getDescription());
        assertEquals(ProjectStatus.ACTIVE, project.getStatus());
    }
}
