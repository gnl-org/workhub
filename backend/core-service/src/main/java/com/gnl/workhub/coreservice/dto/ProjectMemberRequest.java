package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.ProjectRole;
import java.util.UUID;

public record ProjectMemberRequest(
        UUID projectId,
        String userEmail,
        ProjectRole projectRole
) {}