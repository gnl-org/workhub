package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.dto.ProjectRequest;
import com.gnl.workhub.backend.dto.ProjectResponse;
import com.gnl.workhub.backend.dto.UpdateProjectRequest;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.ProjectMember;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.enums.ProjectRole;
import com.gnl.workhub.backend.enums.ProjectStatus;
import com.gnl.workhub.backend.mapper.ProjectMapper;
import com.gnl.workhub.backend.repository.ProjectMemberRepository;
import com.gnl.workhub.backend.repository.ProjectRepository;
import com.gnl.workhub.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private UUID projectId;
    private UUID userId;
    private Project testProject;
    private User testOwner;
    private ProjectRequest projectRequest;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Setup test data
        testOwner = new User();
        testOwner.setId(userId);
        testOwner.setFullName("Project Owner");

        testProject = new Project();
        testProject.setId(projectId);
        testProject.setTitle("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setOwner(testOwner);

        projectRequest = new ProjectRequest();
        projectRequest.setTitle("Test Project");
        projectRequest.setDescription("Test Description");

        projectResponse = new ProjectResponse();
        projectResponse.setId(projectId);
        projectResponse.setTitle("Test Project");
        projectResponse.setDescription("Test Description");
        projectResponse.setStatus(ProjectStatus.ACTIVE);
        projectResponse.setOwnerName("Project Owner");
    }

    @Test
    void testGetAllProjects_Success() {
        // Arrange
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findAll()).thenReturn(projects);

        // Act
        List<ProjectResponse> result = projectService.getAllProjects();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void testGetProjectById_Success() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // Act
        ProjectResponse result = projectService.getProjectById(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(testProject.getTitle(), result.getTitle());
        verify(projectRepository, times(1)).findById(projectId);
    }

    @Test
    void testGetProjectById_NotFound() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> projectService.getProjectById(projectId));
    }

    @Test
    void testCreateProject_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOwner));
        when(projectMapper.toEntity(projectRequest, testOwner)).thenReturn(testProject);
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toResponse(testProject)).thenReturn(projectResponse);

        // Act
        ProjectResponse result = projectService.createProject(projectRequest);

        // Assert
        assertNotNull(result);
        assertEquals(projectResponse.getTitle(), result.getTitle());
        verify(userRepository, times(1)).findById(userId);
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void testCreateProject_OwnerNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> projectService.createProject(projectRequest));
        verify(userRepository, times(1)).findById(userId);
        verify(projectRepository, never()).save(any());
    }

    @Test
    void testGetProjectsByOwnerId_Success() {
        // Arrange
        List<Project> projects = Arrays.asList(testProject);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOwner));
        when(projectRepository.findByOwnerId(userId)).thenReturn(projects);
        when(projectMapper.toResponse(testProject)).thenReturn(projectResponse);

        // Act
        List<ProjectResponse> result = projectService.getProjectsByOwnerId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository, times(1)).findByOwnerId(userId);
    }

    @Test
    void testGetProjectsByOwnerId_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> projectService.getProjectsByOwnerId(userId));
    }

    @Test
    void testGetProjectsByOwnerId_EmptyList() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOwner));
        when(projectRepository.findByOwnerId(userId)).thenReturn(Arrays.asList());

        // Act
        List<ProjectResponse> result = projectService.getProjectsByOwnerId(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetProjectsByUserMembership_Success() {
        // Arrange
        ProjectMember membership = new ProjectMember();
        membership.setProject(testProject);
        membership.setUser(testOwner);

        List<ProjectMember> memberships = Arrays.asList(membership);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testOwner));
        when(projectMemberRepository.findByUserId(userId)).thenReturn(memberships);
        when(projectMapper.toResponse(testProject)).thenReturn(projectResponse);

        // Act
        List<ProjectResponse> result = projectService.getProjectsByUserMembership(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectMemberRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetProjectsByUserMembership_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> projectService.getProjectsByUserMembership(userId));
    }

    @Test
    void testGetProjectsByUserMembership_EmptyList() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testOwner));
        when(projectMemberRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        List<ProjectResponse> result = projectService.getProjectsByUserMembership(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateProject_Success() {
        // Arrange
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle("Updated Project");
        updateRequest.setStatus(ProjectStatus.ARCHIVED);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toResponse(testProject)).thenReturn(projectResponse);

        // Act
        ProjectResponse result = projectService.updateProject(projectId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(projectRepository, times(1)).findById(projectId);
        verify(projectRepository, times(1)).save(testProject);
        verify(projectMapper, times(1)).updateEntityFromRequest(updateRequest, testProject);
    }

    @Test
    void testUpdateProject_ProjectNotFound() {
        // Arrange
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> projectService.updateProject(projectId, updateRequest));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void testUpdateProject_PartialUpdate() {
        // Arrange
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription(null);
        updateRequest.setStatus(null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toResponse(testProject)).thenReturn(projectResponse);

        // Act
        ProjectResponse result = projectService.updateProject(projectId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(projectMapper, times(1)).updateEntityFromRequest(updateRequest, testProject);
    }

    @Test
    void testDeleteProject_Success() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // Act
        projectService.deleteProject(projectId);

        // Assert
        verify(projectRepository, times(1)).findById(projectId);
        verify(projectRepository, times(1)).delete(testProject);
    }

    @Test
    void testDeleteProject_ProjectNotFound() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> projectService.deleteProject(projectId));
        verify(projectRepository, never()).delete(any());
    }
}
