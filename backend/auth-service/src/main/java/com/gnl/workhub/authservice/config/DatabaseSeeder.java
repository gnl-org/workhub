package com.gnl.workhub.authservice.config;

import com.gnl.workhub.authservice.dto.UserEvent;
import com.gnl.workhub.authservice.entity.User;
import com.gnl.workhub.authservice.enums.UserRole;
import com.gnl.workhub.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("seed")
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher eventPublisher;
    private final Faker faker = new Faker();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("Users already exist, skipping seed.");
            return;
        }

        System.out.println("Seeding auth users...");

        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            users.add(createUser("admin" + i + "@workhub.com", UserRole.ADMIN));
        }
        for (int i = 0; i < 50; i++) {
            users.add(createUser(faker.internet().emailAddress(), UserRole.USER));
        }

        userRepository.saveAll(users);

        for (User u : users) {
            eventPublisher.publishUserCreated(new UserEvent(
                    u.getId(), u.getEmail(), u.getFullName(),
                    u.getGlobalRole().name(), "USER_CREATED"
            ));
        }

        System.out.println("Seeded " + users.size() + " auth users with password123.");
    }

    private User createUser(String email, UserRole role) {
        return new User(
                email,
                passwordEncoder.encode("password123"),
                faker.name().firstName(),
                role
        );
    }
}
