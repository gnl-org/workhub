package com.gnl.workhub.backend.mapper;

import com.gnl.workhub.backend.dto.TaskRequest;
import com.gnl.workhub.backend.dto.TaskResponse;
import com.gnl.workhub.backend.dto.UpdateTaskRequest;
import com.gnl.workhub.backend.entity.Project;
import com.gnl.workhub.backend.entity.Task;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.enums.TaskPriority;
import com.gnl.workhub.backend.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperTest {
    private TaskMapper taskMapper;
    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper();

        // Setup test data
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setFullName("John Doe");

        testProject = new Project();
        testProject.setId(UUID.randomUUID());
        testProject.setTitle("Test Project");
    }

    @Test
    void testToEntity_WithValidRequest() {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("Test Task");
        request.setDescription("Test Description");
        request.setStatus(TaskStatus.OPEN);
        request.setPriority(TaskPriority.HIGH);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(5);
        request.setDueDate(dueDate);

        // Act
        Task task = taskMapper.toEntity(request, testProject, testUser);

        // Assert
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(TaskStatus.OPEN, task.getStatus());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertEquals(testProject, task.getProject());
        assertEquals(testUser, task.getAssignedTo());
        assertEquals(dueDate, task.getDueDate());
    }

    @Test
    void testToEntity_WithNullAssignee() {
        // Arrange
        TaskRequest request = new TaskRequest();
        request.setTitle("Unassigned Task");
        request.setDescription("No assignee");
        request.setStatus(TaskStatus.OPEN);
        request.setPriority(TaskPriority.MEDIUM);

        // Act
        Task task = taskMapper.toEntity(request, testProject, null);

        // Assert
        assertEquals("Unassigned Task", task.getTitle());
        assertNull(task.getAssignedTo());
        assertEquals(testProject, task.getProject());
    }

    @Test
    void testToResponse_WithAssignedUser() {
        // Arrange
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.MEDIUM);
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setProject(testProject);
        task.setAssignedTo(testUser);

        // Act
        TaskResponse response = taskMapper.toResponse(task);

        // Assert
        assertEquals(task.getId(), response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
        assertEquals(TaskPriority.MEDIUM, response.getPriority());
        assertEquals("John Doe", response.getAssigneeName());
        assertEquals("Test Project", response.getProjectTitle());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void testToResponse_WithoutAssignedUser() {
        // Arrange
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Unassigned Task");
        task.setDescription("No assignee");
        task.setStatus(TaskStatus.OPEN);
        task.setPriority(TaskPriority.LOW);
        task.setProject(testProject);
        task.setAssignedTo(null);

        // Act
        TaskResponse response = taskMapper.toResponse(task);

        // Assert
        assertEquals("Unassigned", response.getAssigneeName());
        assertEquals("Test Project", response.getProjectTitle());
    }

    @Test
    void testUpdateEntityFromRequest_UpdateAllFields() {
        // Arrange
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Old Title");
        task.setDescription("Old Description");
        task.setStatus(TaskStatus.OPEN);
        task.setPriority(TaskPriority.LOW);

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("New Title");
        request.setDescription("New Description");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setPriority(TaskPriority.HIGH);

        // Act
        taskMapper.updateEntityFromRequest(request, task, testUser);

        // Assert
        assertEquals("New Title", task.getTitle());
        assertEquals("New Description", task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertEquals(testUser, task.getAssignedTo());
    }

    @Test
    void testUpdateEntityFromRequest_UpdatePartialFields() {
        // Arrange
        Task task = new Task();
        task.setTitle("Old Title");
        task.setDescription("Old Description");
        task.setStatus(TaskStatus.OPEN);
        task.setPriority(TaskPriority.LOW);
        task.setAssignedTo(testUser);

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("New Title");
        request.setDescription(null); // Should not update
        request.setStatus(null); // Should not update
        request.setPriority(TaskPriority.CRITICAL);

        // Act
        taskMapper.updateEntityFromRequest(request, task, null);

        // Assert
        assertEquals("New Title", task.getTitle());
        assertEquals("Old Description", task.getDescription()); // Should remain unchanged
        assertEquals(TaskStatus.OPEN, task.getStatus()); // Should remain unchanged
        assertEquals(TaskPriority.CRITICAL, task.getPriority());
        assertNull(task.getAssignedTo()); // Should be unassigned
    }

    @Test
    void testUpdateEntityFromRequest_UnassignTask() {
        // Arrange
        Task task = new Task();
        task.setTitle("Task");
        task.setAssignedTo(testUser);

        UpdateTaskRequest request = new UpdateTaskRequest();

        // Act
        taskMapper.updateEntityFromRequest(request, task, null);

        // Assert
        assertNull(task.getAssignedTo());
    }
}
