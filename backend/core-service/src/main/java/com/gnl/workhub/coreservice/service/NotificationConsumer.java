package com.gnl.workhub.coreservice.service;

import com.gnl.workhub.coreservice.config.RabbitMQConfig;
import com.gnl.workhub.coreservice.dto.NotificationMessage;
import com.gnl.workhub.coreservice.dto.NotificationResponse;
import com.gnl.workhub.coreservice.entity.Notification;
import com.gnl.workhub.coreservice.repository.NotificationRepository;
import com.gnl.workhub.coreservice.repository.ProjectRepository;
import com.gnl.workhub.coreservice.repository.TaskRepository;
import com.gnl.workhub.coreservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleNotification(NotificationMessage message) {
        var notification = Notification.builder()
                .type(message.getType())
                .message(message.getMessage())
                .build();

        if (message.getRecipientId() != null) {
            notification.setUser(userRepository.getReferenceById(message.getRecipientId()));
        }
        if (message.getTaskId() != null) {
            notification.setTask(taskRepository.getReferenceById(message.getTaskId()));
        }
        if (message.getProjectId() != null) {
            notification.setProject(projectRepository.getReferenceById(message.getProjectId()));
        }

        notificationRepository.save(notification);

        if (message.getRecipientId() != null) {
            userRepository.findById(message.getRecipientId()).ifPresent(recipient -> {
                messagingTemplate.convertAndSendToUser(
                        recipient.getEmail(),
                        "/queue/notifications",
                        new NotificationResponse(
                                notification.getId(),
                                notification.getType(),
                                notification.getMessage(),
                                notification.getTask() != null ? notification.getTask().getId() : null,
                                notification.getProject() != null ? notification.getProject().getId() : null,
                                notification.isRead(),
                                notification.getCreatedAt()
                        )
                );
            });
        }
    }
}
