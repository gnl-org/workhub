package com.gnl.workhub.coreservice.repository;

import com.gnl.workhub.coreservice.entity.Notification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @EntityGraph(attributePaths = {"user", "task", "project"})
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(UUID userId, boolean isRead);

    @EntityGraph(attributePaths = {"user", "task", "project"})
    List<Notification> findTop30ByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndIsRead(UUID userId, boolean isRead);
}
