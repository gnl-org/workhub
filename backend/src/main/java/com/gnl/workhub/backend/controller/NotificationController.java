package com.gnl.workhub.backend.controller;

import com.gnl.workhub.backend.dto.NotificationResponse;
import com.gnl.workhub.backend.entity.Notification;
import com.gnl.workhub.backend.repository.NotificationRepository;
import com.gnl.workhub.backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        var user = securityUtil.getCurrentUser();
        return ResponseEntity.ok(
                notificationRepository.findTop30ByUserIdOrderByCreatedAtDesc(user.getId())
                        .stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        var user = securityUtil.getCurrentUser();
        long count = notificationRepository.countByUserIdAndIsRead(user.getId(), false);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        var user = securityUtil.getCurrentUser();
        notificationRepository.findById(id).ifPresent(notification -> {
            if (notification.getUser().getId().equals(user.getId())) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        var user = securityUtil.getCurrentUser();
        var notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(user.getId(), false);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
        return ResponseEntity.ok().build();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.getTask() != null ? n.getTask().getId() : null,
                n.getProject() != null ? n.getProject().getId() : null,
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
