package com.gnl.workhub.coreservice.auth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private java.util.UUID id;
    private String email;
    private String role;
    private String fullName;
}
