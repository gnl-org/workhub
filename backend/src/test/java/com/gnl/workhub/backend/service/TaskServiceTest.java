//package com.gnl.workhub.backend.service;
//
//import com.gnl.workhub.backend.dto.TaskRequest;
//import com.gnl.workhub.backend.dto.TaskResponse;
//import com.gnl.workhub.backend.dto.UpdateTaskRequest;
//import com.gnl.workhub.backend.entity.Project;
//import com.gnl.workhub.backend.entity.Task;
//import com.gnl.workhub.backend.entity.User;
//import com.gnl.workhub.backend.enums.TaskPriority;
//import com.gnl.workhub.backend.enums.TaskStatus;
//import com.gnl.workhub.backend.mapper.TaskMapper;
//import com.gnl.workhub.backend.repository.ProjectRepository;
//import com.gnl.workhub.backend.repository.TaskRepository;
//import com.gnl.workhub.backend.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TaskServiceTest {
//
//    @Mock
//    private TaskRepository taskRepository;
//
//    @Mock
//    private ProjectRepository projectRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private TaskMapper taskMapper;
//
//    @InjectMocks
//    private TaskService taskService;
//
//    private UUID taskId;
//    private UUID projectId;
//    private UUID userId;
//    private Task testTask;
//    private Project testProject;
//    private User testUser;
//    private TaskRequest taskRequest;
//    private TaskResponse taskResponse;
//
//    @BeforeEach
//    void setUp() {
//        taskId = UUID.randomUUID();
//        projectId = UUID.randomUUID();
//        userId = UUID.randomUUID();
//
//        // Setup test data
//        testUser = new User();
//        testUser.setId(userId);
//        testUser.setFullName("John Doe");
//
//        testProject = new Project();
//        testProject.setId(projectId);
//        testProject.setTitle("Test Project");
//
//        testTask = new Task();
//        testTask.setId(taskId);
//        testTask.setTitle("Test Task");
//        testTask.setDescription("Test Description");
//        testTask.setStatus(TaskStatus.OPEN);
//        testTask.setPriority(TaskPriority.HIGH);
//        testTask.setProject(testProject);
//        testTask.setAssignedTo(testUser);
//
//        taskRequest = new TaskRequest();
//        taskRequest.setTitle("Test Task");
//        taskRequest.setDescription("Test Description");
//        taskRequest.setProjectId(projectId);
//        taskRequest.setAssignedToId(userId);
//        taskRequest.setStatus(TaskStatus.OPEN);
//        taskRequest.setPriority(TaskPriority.HIGH);
//
//        taskResponse = new TaskResponse();
//        taskResponse.setId(taskId);
//        taskResponse.setTitle("Test Task");
//        taskResponse.setProjectTitle("Test Project");
//        taskResponse.setAssigneeName("John Doe");
//    }
//
//    @Test
//    void testCreateTask_Success() {
//        // Arrange
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
//        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
//        when(taskMapper.toEntity(taskRequest, testProject, testUser)).thenReturn(testTask);
//        when(taskRepository.save(testTask)).thenReturn(testTask);
//        when(taskMapper.toResponse(testTask)).thenReturn(taskResponse);
//
//        // Act
//        TaskResponse result = taskService.createTask(taskRequest);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(taskResponse.getTitle(), result.getTitle());
//        verify(projectRepository, times(1)).findById(projectId);
//        verify(userRepository, times(1)).findById(userId);
//        verify(taskRepository, times(1)).save(testTask);
//    }
//
//    @Test
//    void testCreateTask_ProjectNotFound() {
//        // Arrange
//        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(RuntimeException.class, () -> taskService.createTask(taskRequest));
//        verify(projectRepository, times(1)).findById(projectId);
//        verify(userRepository, never()).findById(any());
//        verify(taskRepository, never()).save(any());
//    }
//
//    @Test
//    void testCreateTask_UserNotFound() {
//        // Arrange
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
//        when(userRepository.findById(userId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(RuntimeException.class, () -> taskService.createTask(taskRequest));
//        verify(projectRepository, times(1)).findById(projectId);
//        verify(userRepository, times(1)).findById(userId);
//        verify(taskRepository, never()).save(any());
//    }
//
//    @Test
//    void testCreateTask_WithoutAssignee() {
//        // Arrange
//        TaskRequest requestNoAssignee = new TaskRequest();
//        requestNoAssignee.setTitle("Unassigned Task");
//        requestNoAssignee.setProjectId(projectId);
//        requestNoAssignee.setAssignedToId(null);
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
//        when(taskMapper.toEntity(requestNoAssignee, testProject, null)).thenReturn(testTask);
//        when(taskRepository.save(testTask)).thenReturn(testTask);
//        when(taskMapper.toResponse(testTask)).thenReturn(taskResponse);
//
//        // Act
//        TaskResponse result = taskService.createTask(requestNoAssignee);
//
//        // Assert
//        assertNotNull(result);
//        verify(userRepository, never()).findById(any());
//    }
//
//    @Test
//    void testGetTasksByProjectId_Success() {
//        // Arrange
//        List<Task> tasks = Arrays.asList(testTask);
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
//        when(taskRepository.findByProjectId(projectId)).thenReturn(tasks);
//        when(taskMapper.toResponse(testTask)).thenReturn(taskResponse);
//
//        // Act
//        List<TaskResponse> result = taskService.getTasksByProjectId(projectId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(projectRepository, times(1)).findById(projectId);
//        verify(taskRepository, times(1)).findByProjectId(projectId);
//    }
//
//    @Test
//    void testGetTasksByProjectId_ProjectNotFound() {
//        // Arrange
//        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(RuntimeException.class, () -> taskService.getTasksByProjectId(projectId));
//        verify(projectRepository, times(1)).findById(projectId);
//        verify(taskRepository, never()).findByProjectId(any());
//    }
//
//    @Test
//    void testGetTasksByProjectIdAndUserId_Success() {
//        // Arrange
//        List<Task> tasks = Arrays.asList(testTask);
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
//        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
//        when(taskRepository.findByProjectIdAndAssignedToId(projectId, userId)).thenReturn(tasks);
//        when(taskMapper.toResponse(testTask)).thenReturn(taskResponse);
//
//        // Act
//        List<TaskResponse> result = taskService.getTasksByProjectIdAndUserId(projectId, userId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(taskRepository, times(1)).findByProjectIdAndAssignedToId(projectId, userId);
//    }
//
//    @Test
//    void testGetTasksByProjectIdAndUserId_ProjectNotFound() {
//        // Arrange
//        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(RuntimeException.class, () -> taskService.getTasksByProjectIdAndUserId(projectId, userId));
//    }
//
//    @Test
//    void testUpdateTask_Success() {
//        // Arrange
//        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
//        updateRequest.setTitle("Updated Task");
//        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
//        updateRequest.setAssignedToId(userId); // Set assignedToId to fetch user
//
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
//        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
//        when(taskRepository.save(testTask)).thenReturn(testTask);
//        when(taskMapper.toResponse(testTask)).thenReturn(taskResponse);
//
//        // Act
//        TaskResponse result = taskService.updateTask(taskId, updateRequest);
//
//        // Assert
//        assertNotNull(result);
//        verify(taskRepository, times(1)).findById(taskId);
//        verify(taskRepository, times(1)).save(testTask);
//        verify(taskMapper, times(1)).updateEntityFromRequest(updateRequest, testTask, testUser);
//    }
//
//    @Test
//    void testUpdateTask_TaskNotFound() {
//        // Arrange
//        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
//        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(RuntimeException.class, () -> taskService.updateTask(taskId, updateRequest));
//        verify(taskRepository, times(1)).findById(taskId);
//        verify(taskRepository, never()).save(any());
//    }
//
//    @Test
//    void testUpdateTask_WithNullAssignee() {
//        // Arrange
//        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
//        updateRequest.setTitle("Updated Task");
//        updateRequest.setAssignedToId(null); // Unassign
//
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
//        when(taskRepository.save(testTask)).thenReturn(testTask);
//        when(taskMapper.toResponse(testTask)).thenReturn(taskResponse);
//
//        // Act
//        TaskResponse result = taskService.updateTask(taskId, updateRequest);
//
//        // Assert
//        assertNotNull(result);
//        verify(taskMapper, times(1)).updateEntityFromRequest(updateRequest, testTask, null);
//    }
//
//    @Test
//    void testDeleteTask_Success() {
//        // Arrange
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
//
//        // Act
//        taskService.deleteTask(taskId);
//
//        // Assert
//        verify(taskRepository, times(1)).findById(taskId);
//        verify(taskRepository, times(1)).delete(testTask);
//    }
//
//    @Test
//    void testDeleteTask_TaskNotFound() {
//        // Arrange
//        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(RuntimeException.class, () -> taskService.deleteTask(taskId));
//        verify(taskRepository, never()).delete(any());
//    }
//
//    @Test
//    void testGetTaskById_Success() {
//        // Arrange
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
//        when(taskMapper.toResponse(testTask)).thenReturn(taskResponse);
//
//        // Act
//        TaskResponse result = taskService.getTaskById(taskId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(taskResponse.getId(), result.getId());
//        verify(taskRepository, times(1)).findById(taskId);
//    }
//
//    @Test
//    void testGetTaskById_TaskNotFound() {
//        // Arrange
//        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(RuntimeException.class, () -> taskService.getTaskById(taskId));
//    }
//}
