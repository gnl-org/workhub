package com.gnl.workhub.coreservice.config;

import com.gnl.workhub.coreservice.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gnl.workhub.coreservice.config.UserSyncRabbitConfig.USER_QUEUE;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSyncConsumer {

    private final EntityManager entityManager;
    private final UserRepository userRepository;

    @Transactional
    @RabbitListener(queues = USER_QUEUE)
    public void handleUserCreated(Map<String, Object> message) {
        String id = message.get("id").toString();
        String email = (String) message.get("email");
        String fullName = (String) message.get("fullName");
        String role = (String) message.get("role");

        UUID userId = UUID.fromString(id);
        if (userRepository.existsById(userId)) return;

        entityManager.createNativeQuery(
                "INSERT INTO users (id, email, password_hash, full_name, global_role, created_at, updated_at, is_deleted) " +
                "VALUES (?, ?, '', ?, ?, NOW(), NOW(), false)")
                .setParameter(1, userId)
                .setParameter(2, email)
                .setParameter(3, fullName)
                .setParameter(4, role)
                .executeUpdate();
    }
}
