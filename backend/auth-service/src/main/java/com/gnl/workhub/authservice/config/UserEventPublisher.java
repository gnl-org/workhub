package com.gnl.workhub.authservice.config;

import com.gnl.workhub.authservice.dto.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserCreated(UserEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, "user.created", event);
    }
}
