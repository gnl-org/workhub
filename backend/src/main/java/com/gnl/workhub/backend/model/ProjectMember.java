package com.gnl.workhub.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "ProjectMember")
@Data
public class ProjectMember {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Project project;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private ProjectRole projectRole;
}
