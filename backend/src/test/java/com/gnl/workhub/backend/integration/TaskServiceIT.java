//package com.gnl.workhub.backend.integration;
//
//import com.gnl.workhub.backend.dto.TaskRequest;
//import com.gnl.workhub.backend.dto.TaskResponse;
//import com.gnl.workhub.backend.dto.UpdateTaskRequest;
//import com.gnl.workhub.backend.entity.Project;
//import com.gnl.workhub.backend.entity.Task;
//import com.gnl.workhub.backend.entity.User;
//import com.gnl.workhub.backend.enums.TaskPriority;
//import com.gnl.workhub.backend.enums.TaskStatus;
//import com.gnl.workhub.backend.repository.ProjectRepository;
//import com.gnl.workhub.backend.repository.TaskRepository;
//import com.gnl.workhub.backend.repository.UserRepository;
//import com.gnl.workhub.backend.service.TaskService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.*;
//
//class TaskServiceIT extends BaseIntegrationTest {
//
//    @Autowired
//    private TaskService taskService;
//
//    @Autowired
//    private TaskRepository taskRepository;
//
//    @Autowired
//    private ProjectRepository projectRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private User testOwner;
//    private User testAssignee;
//    private Project testProject;
//
//    @BeforeEach
//    void setUp() {
//        // Create test users
//        testOwner = new User();
//        testOwner.setFullName("Project Owner");
//        testOwner.setEmail("owner@test.com");
//        testOwner.setPasswordHash("hash");
//        testOwner = userRepository.save(testOwner);
//
//        testAssignee = new User();
//        testAssignee.setFullName("Task Assignee");
//        testAssignee.setEmail("assignee@test.com");
//        testAssignee.setPasswordHash("hash");
//        testAssignee = userRepository.save(testAssignee);
//
//        // Create test project
//        testProject = new Project();
//        testProject.setTitle("Integration Test Project");
//        testProject.setDescription("For task testing");
//        testProject.setOwner(testOwner);
//        testProject = projectRepository.save(testProject);
//    }
//
//    @Test
//    void shouldCreateTaskAndPersistToDatabase() {
//        // Arrange
//        TaskRequest request = new TaskRequest();
//        request.setTitle("Integration Test Task");
//        request.setDescription("Created via integration test");
//        request.setProjectId(testProject.getId());
//        request.setAssignedToId(testAssignee.getId());
//        request.setStatus(TaskStatus.OPEN);
//        request.setPriority(TaskPriority.HIGH);
//
//        // Act
//        TaskResponse response = taskService.createTask(request);
//
//        // Assert
//        assertThat(response.getId()).isNotNull();
//        assertThat(response.getTitle()).isEqualTo("Integration Test Task");
//        assertThat(response.getDescription()).isEqualTo("Created via integration test");
//        assertThat(response.getAssigneeName()).isEqualTo("Task Assignee");
//        assertThat(response.getProjectTitle()).isEqualTo("Integration Test Project");
//        assertThat(response.getStatus()).isEqualTo(TaskStatus.OPEN);
//        assertThat(response.getPriority()).isEqualTo(TaskPriority.HIGH);
//
//        // Verify in database
//        Optional<Task> savedTask = taskRepository.findById(response.getId());
//        assertThat(savedTask).isPresent();
//        assertThat(savedTask.get().getTitle()).isEqualTo("Integration Test Task");
//    }
//
//    @Test
//    void shouldCreateUnassignedTask() {
//        // Arrange
//        TaskRequest request = new TaskRequest();
//        request.setTitle("Unassigned Task");
//        request.setProjectId(testProject.getId());
//        request.setAssignedToId(null); // No assignee
//        request.setStatus(TaskStatus.OPEN);
//        request.setPriority(TaskPriority.MEDIUM);
//
//        // Act
//        TaskResponse response = taskService.createTask(request);
//
//        // Assert
//        assertThat(response.getAssigneeName()).isEqualTo("Unassigned");
//        assertThat(taskRepository.findById(response.getId()).get().getAssignedTo()).isNull();
//    }
//
//    @Test
//    void shouldRetrieveAllTasksInProject() {
//        // Arrange: Create 5 tasks
//        for (int i = 1; i <= 5; i++) {
//            TaskRequest request = new TaskRequest();
//            request.setTitle("Task " + i);
//            request.setProjectId(testProject.getId());
//            request.setStatus(TaskStatus.OPEN);
//            request.setPriority(TaskPriority.MEDIUM);
//            taskService.createTask(request);
//        }
//
//        // Act
//        List<TaskResponse> tasks = taskService.getTasksByProjectId(testProject.getId());
//
//        // Assert
//        assertThat(tasks)
//                .hasSize(5)
//                .allMatch(t -> t.getProjectTitle().equals("Integration Test Project"))
//                .extracting(TaskResponse::getTitle)
//                .contains("Task 1", "Task 2", "Task 3", "Task 4", "Task 5");
//    }
//
//    @Test
//    void shouldRetrieveTasksAssignedToUser() {
//        // Arrange: Create tasks assigned to testAssignee and others
//        TaskRequest assignedRequest = new TaskRequest();
//        assignedRequest.setTitle("Assigned Task 1");
//        assignedRequest.setProjectId(testProject.getId());
//        assignedRequest.setAssignedToId(testAssignee.getId());
//        assignedRequest.setStatus(TaskStatus.OPEN);
//        taskService.createTask(assignedRequest);
//
//        TaskRequest unassignedRequest = new TaskRequest();
//        unassignedRequest.setTitle("Unassigned Task");
//        unassignedRequest.setProjectId(testProject.getId());
//        unassignedRequest.setStatus(TaskStatus.OPEN);
//        taskService.createTask(unassignedRequest);
//
//        // Act
//        List<TaskResponse> tasksForUser = taskService.getTasksByProjectIdAndUserId(
//                testProject.getId(),
//                testAssignee.getId()
//        );
//
//        // Assert
//        assertThat(tasksForUser)
//                .hasSize(1)
//                .allMatch(t -> t.getAssigneeName().equals("Task Assignee"))
//                .extracting(TaskResponse::getTitle)
//                .contains("Assigned Task 1");
//    }
//
//    @Test
//    void shouldUpdateTaskWithPartialChanges() {
//        // Arrange: Create task
//        TaskRequest initialRequest = new TaskRequest();
//        initialRequest.setTitle("Original Title");
//        initialRequest.setDescription("Original Description");
//        initialRequest.setProjectId(testProject.getId());
//        initialRequest.setStatus(TaskStatus.OPEN);
//        initialRequest.setPriority(TaskPriority.LOW);
//        TaskResponse createdTask = taskService.createTask(initialRequest);
//
//        // Act: Update only title and status
//        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
//        updateRequest.setTitle("Updated Title");
//        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
//
//        TaskResponse updatedTask = taskService.updateTask(createdTask.getId(), updateRequest);
//
//        // Assert
//        assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
//        assertThat(updatedTask.getDescription()).isEqualTo("Original Description"); // Should remain unchanged
//        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
//        assertThat(updatedTask.getPriority()).isEqualTo(TaskPriority.LOW); // Should remain unchanged
//
//        // Verify in database
//        Task dbTask = taskRepository.findById(createdTask.getId()).orElseThrow();
//        assertThat(dbTask.getTitle()).isEqualTo("Updated Title");
//        assertThat(dbTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
//    }
//
//    @Test
//    void shouldUnassignTaskDuringUpdate() {
//        // Arrange: Create assigned task
//        TaskRequest request = new TaskRequest();
//        request.setTitle("Assigned Task");
//        request.setProjectId(testProject.getId());
//        request.setAssignedToId(testAssignee.getId());
//        request.setStatus(TaskStatus.OPEN);
//        TaskResponse createdTask = taskService.createTask(request);
//
//        // Verify it's assigned
//        assertThat(createdTask.getAssigneeName()).isEqualTo("Task Assignee");
//
//        // Act: Unassign by setting assignedToId to null
//        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
//        updateRequest.setAssignedToId(null);
//
//        TaskResponse updatedTask = taskService.updateTask(createdTask.getId(), updateRequest);
//
//        // Assert
//        assertThat(updatedTask.getAssigneeName()).isEqualTo("Unassigned");
//        assertThat(taskRepository.findById(createdTask.getId()).get().getAssignedTo()).isNull();
//    }
//
//    @Test
//    void shouldDeleteTask() {
//        // Arrange: Create task
//        TaskRequest request = new TaskRequest();
//        request.setTitle("Task to Delete");
//        request.setProjectId(testProject.getId());
//        request.setStatus(TaskStatus.OPEN);
//        TaskResponse createdTask = taskService.createTask(request);
//
//        UUID taskId = createdTask.getId();
//
//        // Verify task exists
//        assertThat(taskRepository.findById(taskId)).isPresent();
//
//        // Act
//        taskService.deleteTask(taskId);
//
//        // Assert
//        assertThat(taskRepository.findById(taskId)).isEmpty();
//    }
//
//    @Test
//    void shouldThrowExceptionWhenCreatingTaskWithNonExistentProject() {
//        // Arrange
//        TaskRequest request = new TaskRequest();
//        request.setTitle("Invalid Task");
//        request.setProjectId(UUID.randomUUID()); // Non-existent project
//
//        // Act & Assert
//        assertThatThrownBy(() -> taskService.createTask(request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Project not found");
//    }
//
//    @Test
//    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
//        // Act & Assert
//        UpdateTaskRequest request = new UpdateTaskRequest();
//        request.setTitle("Updated");
//
//        assertThatThrownBy(() -> taskService.updateTask(UUID.randomUUID(), request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Task not found");
//    }
//}
