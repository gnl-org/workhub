package com.gnl.workhub.coreservice.config;

import com.gnl.workhub.coreservice.entity.User;
import com.gnl.workhub.coreservice.enums.UserRole;
import com.gnl.workhub.coreservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gnl.workhub.coreservice.config.UserSyncRabbitConfig.USER_QUEUE;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSyncConsumer {

    private final UserRepository userRepository;

    @Transactional
    @RabbitListener(queues = USER_QUEUE)
    public void handleUserCreated(Map<String, Object> message) {
        String id = message.get("id").toString();
        String email = (String) message.get("email");
        String fullName = (String) message.get("fullName");
        String role = (String) message.get("role");

        if (userRepository.findById(UUID.fromString(id)).isPresent()) return;

        var user = User.builder()
                .email(email)
                .fullName(fullName)
                .globalRole(UserRole.valueOf(role))
                .build();
        user.setId(UUID.fromString(id));
        userRepository.save(user);
    }
}
