package com.gnl.workhub.notificationservice.controller;

import com.gnl.workhub.notificationservice.dto.NotificationResponse;
import com.gnl.workhub.notificationservice.entity.Notification;
import com.gnl.workhub.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        var userId = getCurrentUserId();
        return ResponseEntity.ok(
                notificationRepository.findTop30ByUserIdOrderByCreatedAtDesc(userId)
                        .stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        var userId = getCurrentUserId();
        long count = notificationRepository.countByUserIdAndIsRead(userId, false);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        var userId = getCurrentUserId();
        notificationRepository.findById(id).ifPresent(notification -> {
            if (notification.getUserId().equals(userId)) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        var userId = getCurrentUserId();
        var notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
        return ResponseEntity.ok().build();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.getTaskId(),
                n.getProjectId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }

    private UUID getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(auth.getName());
    }
}
