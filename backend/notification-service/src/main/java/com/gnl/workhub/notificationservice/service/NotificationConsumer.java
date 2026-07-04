package com.gnl.workhub.notificationservice.service;

import com.gnl.workhub.notificationservice.config.RabbitMQConfig;
import com.gnl.workhub.notificationservice.dto.NotificationMessage;
import com.gnl.workhub.notificationservice.dto.NotificationResponse;
import com.gnl.workhub.notificationservice.entity.Notification;
import com.gnl.workhub.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

// TODO: When a dedicated gateway is introduced, push WebSocket directly from
// notification-service instead of routing through core-service
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final RestTemplate restTemplate;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleNotification(NotificationMessage message) {
        var notification = Notification.builder()
                .userId(message.getRecipientId())
                .taskId(message.getTaskId())
                .projectId(message.getProjectId())
                .type(message.getType())
                .message(message.getMessage())
                .build();

        notificationRepository.save(notification);

        if (message.getRecipientEmail() != null) {
            var pushBody = new WsPushRequest(
                    message.getRecipientEmail(),
                    new NotificationResponse(
                            notification.getId(),
                            notification.getType(),
                            notification.getMessage(),
                            notification.getTaskId(),
                            notification.getProjectId(),
                            notification.isRead(),
                            notification.getCreatedAt()
                    )
            );
            try {
                restTemplate.postForEntity("http://localhost:8080/api/v1/internal/ws-push", pushBody, Void.class);
            } catch (Exception e) {
                // WebSocket push failure is non-critical
            }
        }
    }

    private record WsPushRequest(String recipientEmail, NotificationResponse notification) {}
}
