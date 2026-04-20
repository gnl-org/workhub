package com.gnl.workhub.backend.controller;

import com.gnl.workhub.backend.dto.ProjectMemberRequest;
import com.gnl.workhub.backend.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projectMember")
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    @PostMapping("/addMember")
    public void addMemberToProject(@RequestBody ProjectMemberRequest request) {
        projectMemberService.addMemberToProject(request);
    }

}
