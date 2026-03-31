package com.gnl.workhub.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}
