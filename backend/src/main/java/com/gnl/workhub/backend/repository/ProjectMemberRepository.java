package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.ProjectMember;
import com.gnl.workhub.backend.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    // Find all users who are members of a specific project
    List<ProjectMember> findByProjectId(UUID projectId);

    // Find all projects a specific user belongs to
    List<ProjectMember> findByUserId(UUID userId);
}
