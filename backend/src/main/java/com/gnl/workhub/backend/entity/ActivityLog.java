package com.gnl.workhub.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activity_logs")
@Data
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String action;   // e.g., "TASK_STATUS_UPDATED", "MEMBER_ADDED"
    private String details;  // e.g., "Changed status from OPEN to IN_PROGRESS"

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project; // Always present

    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;       // NULL if the action is project-level (like adding a member)

    @ManyToOne(fetch = FetchType.LAZY)
    private User performedBy;

    private LocalDateTime timestamp = LocalDateTime.now();
}