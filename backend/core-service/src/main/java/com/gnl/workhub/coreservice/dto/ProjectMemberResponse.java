package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.ProjectRole;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProjectMemberResponse(
        UUID userId,
        String userEmail,
        String userName
) {}