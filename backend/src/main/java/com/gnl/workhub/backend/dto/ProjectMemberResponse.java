package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.ProjectRole;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProjectMemberResponse(
        String userEmail,
        String userName
) {}