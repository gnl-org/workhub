package com.gnl.workhub.notificationservice.service;

import com.gnl.workhub.notificationservice.config.RabbitMQConfig;
import com.gnl.workhub.notificationservice.dto.NotificationMessage;
import com.gnl.workhub.notificationservice.dto.NotificationResponse;
import com.gnl.workhub.notificationservice.entity.Notification;
import com.gnl.workhub.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
            var response = new NotificationResponse(
                    notification.getId(),
                    notification.getType(),
                    notification.getMessage(),
                    notification.getTaskId(),
                    notification.getProjectId(),
                    notification.isRead(),
                    notification.getCreatedAt()
            );
            messagingTemplate.convertAndSendToUser(
                    message.getRecipientEmail(),
                    "/queue/notifications",
                    response
            );
        }
    }
}
