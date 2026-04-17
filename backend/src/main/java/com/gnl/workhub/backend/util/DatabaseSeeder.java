package com.gnl.workhub.backend.util;

import com.gnl.workhub.backend.entity.*;
import com.gnl.workhub.backend.enums.*;
import com.gnl.workhub.backend.repository.*;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("seed") // Only runs if you add --spring.profiles.active=seed
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository; // Added
    private final PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        System.out.println("🌱 Seeding started...");

        // 1. Create Admins
        List<User> admins = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            admins.add(createUser("admin" + i + "@workhub.com", UserRole.ADMIN));
        }

        // 2. Create 50 Users
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            users.add(createUser(faker.internet().emailAddress(), UserRole.USER));
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

            Task task = Task.builder()
                    .title(faker.job().title() + " #" + i)
                    .description(faker.lorem().paragraph())
                    .status(TaskStatus.values()[faker.random().nextInt(TaskStatus.values().length)])
                    .priority(TaskPriority.values()[faker.random().nextInt(TaskPriority.values().length)])
                    .project(project)
                    .createdBy(creator)
                    .assignedTo(assignee)
                    .dueDate(LocalDateTime.now().plusDays(faker.random().nextInt(1, 30)))
                    .build();

            taskRepository.save(task);
        }

        System.out.println("✅ Successfully seeded 300 tasks with valid memberships!");
    }

    private User createUser(String email, UserRole role) {
        return userRepository.save(User.builder()
                .fullName(faker.name().firstName())
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .globalRole(role)
                .build());
    }
}