package com.gnl.workhub.backend.service;

import com.gnl.workhub.backend.config.RabbitMQConfig;
import com.gnl.workhub.backend.dto.NotificationMessage;
import com.gnl.workhub.backend.entity.Notification;
import com.gnl.workhub.backend.repository.NotificationRepository;
import com.gnl.workhub.backend.repository.ProjectRepository;
import com.gnl.workhub.backend.repository.TaskRepository;
import com.gnl.workhub.backend.repository.UserRepository;
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
                        message
                );
            });
        }
    }
}
