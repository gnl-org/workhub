package com.gnl.workhub.backend.repository;

import com.gnl.workhub.backend.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMember.ProjectMemberId> {
    // Find all users who are members of a specific project
    List<ProjectMember> findByProjectId(UUID projectId);

    // Find all projects a specific user belongs to
    List<ProjectMember> findByUserId(UUID userId);

    // Manual JPQL Query to fetch User and Member record in 1 hit
    @Query("SELECT pm FROM ProjectMember pm " +
            "JOIN FETCH pm.user " +
            "WHERE pm.project.id = :projectId")
    List<ProjectMember> findMembersByProjectId(@Param("projectId") UUID projectId);
}
