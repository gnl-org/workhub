package com.gnl.workhub.backend.entity;

import com.gnl.workhub.backend.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberId implements Serializable {
    private UUID project;
    private UUID user;
}

@Entity
@Table(name = "project_members")
@Data
@IdClass(ProjectMemberId.class)
public class ProjectMember {
    @Id
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_role")
    private ProjectRole projectRole;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
}
