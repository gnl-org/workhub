package com.gnl.workhub.coreservice.service;

import com.gnl.workhub.coreservice.config.RabbitMQConfig;
import com.gnl.workhub.coreservice.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(NotificationMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                "notification." + message.getType().name().toLowerCase().replace("_", "."),
                message
        );
    }
}
