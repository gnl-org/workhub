package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.ProjectRole;
import java.util.UUID;

public record ProjectMemberRequest(
        UUID projectId,
        String userEmail,
        ProjectRole projectRole
) {}