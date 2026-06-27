package com.gnl.workhub.backend.controller;

import com.gnl.workhub.backend.dto.*;
import com.gnl.workhub.backend.service.WorkStageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WorkStageController {

    private final WorkStageService workStageService;

    @GetMapping("/projects/{projectId}/work-stages")
    public List<WorkStageResponse> listWorkStages(@PathVariable UUID projectId) {
        return workStageService.listVisibleStages(projectId);
    }

    @PostMapping("/projects/{projectId}/work-stages")
    public WorkStageResponse createWorkStage(@PathVariable UUID projectId,
                                              @Valid @RequestBody CreateWorkStageRequest request) {
        return workStageService.createStage(projectId, request);
    }

    @PatchMapping("/projects/{projectId}/work-stages/{stageId}")
    public WorkStageResponse updateWorkStage(@PathVariable UUID projectId,
                                              @PathVariable UUID stageId,
                                              @Valid @RequestBody UpdateWorkStageRequest request) {
        return workStageService.renameStage(projectId, stageId, request);
    }

    @DeleteMapping("/projects/{projectId}/work-stages/{stageId}")
    public ResponseEntity<Void> deleteWorkStage(@PathVariable UUID projectId,
                                                @PathVariable UUID stageId) {
        workStageService.deleteStage(projectId, stageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/projects/{projectId}/work-stages/reorder")
    public ResponseEntity<Void> reorderWorkStages(@PathVariable UUID projectId,
                                                   @RequestBody ReorderWorkStagesRequest request) {
        workStageService.reorderStages(projectId, request.getStageIds());
        return ResponseEntity.ok().build();
    }
}
