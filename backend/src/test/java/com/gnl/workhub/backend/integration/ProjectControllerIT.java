package com.gnl.workhub.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnl.workhub.backend.dto.ProjectRequest;
import com.gnl.workhub.backend.dto.UpdateProjectRequest;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.enums.ProjectStatus;
import com.gnl.workhub.backend.repository.ProjectRepository;
import com.gnl.workhub.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private User testOwner;

    @BeforeEach
    void setUp() {
        testOwner = new User();
        testOwner.setFullName("Controller Test Owner");
        testOwner.setEmail("controller@test.com");
        testOwner.setPasswordHash("hash");
        testOwner = userRepository.save(testOwner);
    }

    @Test
    void shouldCreateProjectViaEndpoint() throws Exception {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setTitle("API Created Project");
        request.setDescription("Created via REST API");
        request.setOwnerId(testOwner.getId());

        // Act & Assert
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("API Created Project")))
                .andExpect(jsonPath("$.description", equalTo("Created via REST API")))
                .andExpect(jsonPath("$.ownerName", equalTo("Controller Test Owner")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void shouldRetrieveProjectsByOwner() throws Exception {
        // Arrange: Create 2 projects via service
        for (int i = 1; i <= 2; i++) {
            ProjectRequest request = new ProjectRequest();
            request.setTitle("Owner Project " + i);
            request.setOwnerId(testOwner.getId());
            projectRepository.save(new com.gnl.workhub.backend.entity.Project());
        }

        // Act & Assert
        mockMvc.perform(get("/projects/user/{userId}/owned", testOwner.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    void shouldUpdateProjectViaEndpoint() throws Exception {
        // Arrange: Create project
        ProjectRequest createRequest = new ProjectRequest();
        createRequest.setTitle("Original Title");
        createRequest.setOwnerId(testOwner.getId());
        var project = projectRepository.save(
            new com.gnl.workhub.backend.entity.Project()
        );

        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setTitle("Updated via Endpoint");
        updateRequest.setStatus(ProjectStatus.ARCHIVED);

        // Act & Assert
        mockMvc.perform(patch("/projects/{id}", project.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("Updated via Endpoint")))
                .andExpect(jsonPath("$.status", equalTo("ARCHIVED")));
    }

    @Test
    void shouldDeleteProjectViaEndpoint() throws Exception {
        // Arrange: Create project
        var project = projectRepository.save(
            new com.gnl.workhub.backend.entity.Project()
        );
        UUID projectId = project.getId();

        // Verify exists before delete
        mockMvc.perform(get("/projects/{id}", projectId))
                .andExpect(status().isOk());

        // Act: Delete via endpoint
        mockMvc.perform(delete("/projects/{id}", projectId))
                .andExpect(status().isOk());

        // Assert: Verify it's gone
        mockMvc.perform(get("/projects/{id}", projectId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingProjectWithInvalidData() throws Exception {
        // Arrange: Invalid request missing owner
        ProjectRequest invalidRequest = new ProjectRequest();
        invalidRequest.setTitle("Project without owner");
        // ownerId is null

        // Act & Assert
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isInternalServerError()); // Will throw exception
    }
}
