package hu.kornel.server.presentation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.kornel.server.application.dto.assignments.AddExerciseRequest;
import hu.kornel.server.application.dto.assignments.AssignToGroupRequest;
import hu.kornel.server.application.dto.assignments.AssignmentResponse;
import hu.kornel.server.application.dto.assignments.AssignmentSummaryResponse;
import hu.kornel.server.application.dto.assignments.CreateAssignmentRequest;
import hu.kornel.server.application.dto.assignments.ExerciseResponse;
import hu.kornel.server.application.dto.assignments.UpdateAssignmentRequest;
import hu.kornel.server.application.dto.assignments.UpdateExerciseRequest;
import hu.kornel.server.application.usecase.assignments.AddExerciseUseCase;
import hu.kornel.server.application.usecase.assignments.AssignToGroupUseCase;
import hu.kornel.server.application.usecase.assignments.CreateAssignmentUseCase;
import hu.kornel.server.application.usecase.assignments.DeleteAssignmentUseCase;
import hu.kornel.server.application.usecase.assignments.GetAssignmentDetailsUseCase;
import hu.kornel.server.application.usecase.assignments.GetTeacherAssignmentsUseCase;
import hu.kornel.server.application.usecase.assignments.PreviewAssignmentUseCase;
import hu.kornel.server.application.usecase.assignments.RevertToDraftUseCase;
import hu.kornel.server.application.usecase.assignments.SubmitAssignmentUseCase;
import hu.kornel.server.application.usecase.assignments.UnassignFromGroupUseCase;
import hu.kornel.server.application.usecase.assignments.UpdateAssignmentUseCase;
import hu.kornel.server.application.usecase.assignments.UpdateExerciseUseCase;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.infrastructure.security.UserPrincipal;
import hu.kornel.server.presentation.mapper.AssignmentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Slf4j
public class AssignmentController {

    private final CreateAssignmentUseCase createAssignmentUseCase;
    private final UpdateAssignmentUseCase updateAssignmentUseCase;
    private final AddExerciseUseCase addExerciseUseCase;
    private final UpdateExerciseUseCase updateExerciseUseCase;
    private final SubmitAssignmentUseCase submitAssignmentUseCase;
    private final RevertToDraftUseCase revertToDraftUseCase;
    private final GetTeacherAssignmentsUseCase getTeacherAssignmentsUseCase;
    private final GetAssignmentDetailsUseCase getAssignmentDetailsUseCase;
    private final DeleteAssignmentUseCase deleteAssignmentUseCase;
    private final AssignToGroupUseCase assignToGroupUseCase;
    private final UnassignFromGroupUseCase unassignFromGroupUseCase;
    private final AssignmentMapper assignmentMapper;
    private final PreviewAssignmentUseCase previewAssignmentUseCase;

    @PostMapping
    public ResponseEntity<AssignmentResponse> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("POST /api/assignments - Creating assignment: {}", request.getTitle());

        Assignment assignment = createAssignmentUseCase.execute(request, userPrincipal.getId());
        AssignmentResponse response = assignmentMapper.toResponse(assignment);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AssignmentSummaryResponse>> getTeacherAssignments(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/assignments - Fetching assignments for teacher");

        List<Assignment> assignments = getTeacherAssignmentsUseCase.execute(userPrincipal.getId());
        List<AssignmentSummaryResponse> response = assignmentMapper.toSummaryResponseList(assignments);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/assignments/{} - Fetching assignment details", id);

        Assignment assignment = getAssignmentDetailsUseCase.execute(id, userPrincipal.getId());
        AssignmentResponse response = assignmentMapper.toResponse(assignment);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("PUT /api/assignments/{} - Updating assignment", id);

        Assignment assignment = updateAssignmentUseCase.execute(id, request, userPrincipal.getId());
        AssignmentResponse response = assignmentMapper.toResponse(assignment);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/exercises")
    public ResponseEntity<ExerciseResponse> addExercise(
            @PathVariable Long id,
            @Valid @RequestBody AddExerciseRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("POST /api/assignments/{}/exercises - Adding exercise", id);

        Exercise exercise = addExerciseUseCase.execute(id, request, userPrincipal.getId());
        ExerciseResponse response = assignmentMapper.toExerciseResponse(exercise);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{assignmentId}/exercises/{exerciseId}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @PathVariable Long assignmentId,
            @PathVariable Long exerciseId,
            @Valid @RequestBody UpdateExerciseRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("PUT /api/assignments/{}/exercises/{} - Updating exercise", assignmentId, exerciseId);

        Exercise exercise = updateExerciseUseCase.execute(assignmentId, exerciseId, request, userPrincipal.getId());
        ExerciseResponse response = assignmentMapper.toExerciseResponse(exercise);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<AssignmentResponse> submitAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("POST /api/assignments/{}/submit - Submitting assignment", id);

        Assignment assignment = submitAssignmentUseCase.execute(id, userPrincipal.getId());
        AssignmentResponse response = assignmentMapper.toResponse(assignment);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/revert-to-draft")
    public ResponseEntity<AssignmentResponse> revertToDraft(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("POST /api/assignments/{}/revert-to-draft - Reverting assignment to draft", id);

        Assignment assignment = revertToDraftUseCase.execute(id, userPrincipal.getId());
        AssignmentResponse response = assignmentMapper.toResponse(assignment);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("DELETE /api/assignments/{} - Deleting assignment", id);

        deleteAssignmentUseCase.execute(id, userPrincipal.getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<Void> assignToGroups(
            @PathVariable Long id,
            @Valid @RequestBody AssignToGroupRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("POST /api/assignments/{}/assign - Assigning to {} groups", id, request.getGroupIds().size());

        assignToGroupUseCase.execute(id, request.getGroupIds(), userPrincipal.getId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{assignmentId}/groups/{groupId}")
    public ResponseEntity<Void> unassignFromGroup(
            @PathVariable Long assignmentId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("DELETE /api/assignments/{}/groups/{} - Unassigning from group", assignmentId, groupId);

        unassignFromGroupUseCase.execute(assignmentId, groupId, userPrincipal.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<AssignmentResponse> previewAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("GET /api/assignments/{}/preview - Previewing assignment", id);

        Assignment assignment = previewAssignmentUseCase.execute(id, userPrincipal.getId());
        AssignmentResponse response = assignmentMapper.toResponse(assignment);

        return ResponseEntity.ok(response);
    }
}