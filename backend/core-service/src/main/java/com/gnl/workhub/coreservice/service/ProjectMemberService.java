package com.gnl.workhub.coreservice.service;

import com.gnl.workhub.coreservice.dto.ProjectMemberRequest;
import com.gnl.workhub.coreservice.dto.ProjectMemberResponse;
import com.gnl.workhub.coreservice.entity.Project;
import com.gnl.workhub.coreservice.entity.ProjectMember;
import com.gnl.workhub.coreservice.entity.User;
import com.gnl.workhub.coreservice.enums.UserRole;
import com.gnl.workhub.coreservice.repository.ProjectMemberRepository;
import com.gnl.workhub.coreservice.repository.ProjectRepository;
import com.gnl.workhub.coreservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addMemberToProject(ProjectMemberRequest request) {
        // 1. Get the Inviter (Logged-in User)
        String inviterEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User inviter = userRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        // 2. Find the Project
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // 3. Security Check: Only Owner or ADMIN can add members
        boolean isOwner = project.getOwner().getId().equals(inviter.getId());
        boolean isAdmin = inviter.getGlobalRole().equals(UserRole.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to add members to this project");
        }

        // 4. Find the Invitee (The person being added)
        User invitee = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new RuntimeException("User to invite not found"));

        // 5. Create the Membership
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(invitee);
        member.setProjectRole(request.projectRole());

        projectMemberRepository.save(member);
    }

    public List<ProjectMemberResponse> getMembers(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<ProjectMemberResponse> result = new ArrayList<>(projectMemberRepository.findMembersByProjectId(projectId)
                .stream()
                .map(m -> ProjectMemberResponse.builder()
                        .userId(m.getUser().getId())
                        .userName(m.getUser().getFullName())
                        .userEmail(m.getUser().getEmail())
                        .build())
                .toList());

        boolean ownerIncluded = result.stream().anyMatch(m -> m.userId().equals(project.getOwner().getId()));
        if (!ownerIncluded) {
            result.add(ProjectMemberResponse.builder()
                    .userId(project.getOwner().getId())
                    .userName(project.getOwner().getFullName())
                    .userEmail(project.getOwner().getEmail())
                    .build());
        }

        return result;
    }
}