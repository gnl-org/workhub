package com.gnl.workhub.coreservice.entity;

import com.gnl.workhub.coreservice.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "global_role", nullable = false)
    private UserRole globalRole = UserRole.USER;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
