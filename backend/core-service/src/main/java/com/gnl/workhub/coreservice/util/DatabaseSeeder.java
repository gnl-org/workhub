package com.gnl.workhub.coreservice.util;

import com.gnl.workhub.coreservice.entity.*;
import com.gnl.workhub.coreservice.enums.*;
import com.gnl.workhub.coreservice.repository.*;
import com.gnl.workhub.coreservice.service.WorkStageService;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("seed") // Only runs if you add --spring.profiles.active=seed
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkStageService workStageService;
    private final WorkStageRepository workStageRepository;
    private final SprintRepository sprintRepository;
    private final Faker faker = new Faker();

    @Override
    @Transactional
    public void run(String... args) {
        if (projectRepository.count() > 0) {
            System.out.println("Projects already exist, skipping seed.");
            return;
        }

        System.out.println("🌱 Seeding started...");

        // Use users already synced from auth via RabbitMQ
        List<User> allUsers = userRepository.findAll();
        if (allUsers.size() < 3) {
            System.out.println("❌ Need at least 3 users in core DB. Run auth seeder first.");
            return;
        }

        List<User> admins = allUsers.stream()
                .filter(u -> u.getGlobalRole() == UserRole.ADMIN)
                .toList();
        if (admins.isEmpty()) {
            admins = allUsers.subList(0, Math.min(3, allUsers.size()));
        }

        List<User> users = allUsers.stream()
                .filter(u -> u.getGlobalRole() == UserRole.USER)
                .toList();
        if (users.isEmpty()) {
            users = allUsers;
        }

        // 3. Create 6 Projects & Assign Members
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            User owner = admins.get(faker.random().nextInt(admins.size()));

            Project project = new Project();
            project.setTitle("Project " + faker.commerce().productName());
            project.setDescription(faker.lorem().sentence());
            project.setOwner(owner);

            // CRITICAL FIX: Save the project first so it gets a UUID!
            project = projectRepository.save(project);
            workStageService.seedDefaultStages(project);
            projects.add(project);

            // Now it's safe to add the Owner as a member too (Best practice)
            ProjectMember ownerMember = new ProjectMember();
            ownerMember.setProject(project);
            ownerMember.setUser(owner);
            ownerMember.setProjectRole(ProjectRole.OWNER); // Use your high-level role
            projectMemberRepository.save(ownerMember);

            // Add 10 random users to each project as members
            Set<User> projectUsers = new HashSet<>();
            while (projectUsers.size() < 10) {
                projectUsers.add(users.get(faker.random().nextInt(users.size())));
            }

            for (User member : projectUsers) {
                ProjectMember pm = new ProjectMember();
                pm.setProject(project);
                pm.setUser(member);
                pm.setProjectRole(ProjectRole.MEMBER);
                projectMemberRepository.save(pm);
            }
        }

        // 4. Create 300 Tasks (Only assign to people who are members of that project)
        for (int i = 0; i < 300; i++) {
            Project project = projects.get(faker.random().nextInt(projects.size()));

            // Get members of THIS project to ensure validation passes
            List<ProjectMember> members = projectMemberRepository.findAll()
                    .stream()
                    .filter(m -> m.getProject().getId().equals(project.getId()))
                    .toList();

            User creator = members.get(faker.random().nextInt(members.size())).getUser();
            User assignee = members.get(faker.random().nextInt(members.size())).getUser();

            WorkStage backlogStage = workStageRepository.findDefaultBacklogStage(project.getId())
                    .orElseThrow(() -> new RuntimeException("Backlog stage not found for project " + project.getId()));

            Task task = Task.builder()
                    .title(faker.job().title() + " #" + i)
                    .description(faker.lorem().paragraph())
                    .status(TaskStatus.values()[faker.random().nextInt(TaskStatus.values().length)])
                    .priority(TaskPriority.values()[faker.random().nextInt(TaskPriority.values().length)])
                    .project(project)
                    .owner(creator)
                    .assignedTo(assignee)
                    .workStage(backlogStage)
                    .dueDate(LocalDateTime.now().plusDays(faker.random().nextInt(1, 30)))
                    .build();

            taskRepository.save(task);
        }

        // 5. Create 1 planned sprint per project with some tasks assigned
        for (Project project : projects) {
            Sprint sprint = new Sprint();
            sprint.setProject(project);
            sprint.setName("Sprint " + (faker.random().nextInt(1, 5)));
            sprint.setGoal(faker.lorem().sentence());
            sprint.setStatus(SprintStatus.PLANNED);
            sprint.setStartDate(LocalDate.now().minusDays(faker.random().nextInt(1, 10)));
            sprint.setEndDate(LocalDate.now().plusDays(faker.random().nextInt(10, 20)));
            Sprint savedSprint = sprintRepository.save(sprint);

            WorkStage sprintStage = workStageService.createSprintStage(project, savedSprint);

            List<Task> projectTasks = taskRepository.findByProjectId(project.getId()).stream()
                    .filter(t -> !t.isDeleted())
                    .limit(10)
                    .toList();

            for (int i = 0; i < projectTasks.size(); i++) {
                Task task = projectTasks.get(i);
                task.setSprint(savedSprint);
                task.setWorkStage(sprintStage);
                task.setSortOrder(i);
            }
            taskRepository.saveAll(projectTasks);
        }

        System.out.println("✅ Seeded " + projects.size() + " sprints with tasks!");
        System.out.println("✅ Successfully seeded 300 tasks with valid memberships!");
    }

}