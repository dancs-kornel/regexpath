package hu.kornel.server.presentation.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.kornel.server.application.dto.assignments.AssignmentResponse;
import hu.kornel.server.application.dto.assignments.AssignmentSummaryResponse;
import hu.kornel.server.application.dto.assignments.AttemptResponse;
import hu.kornel.server.application.dto.assignments.AttemptSummaryResponse;
import hu.kornel.server.application.dto.assignments.SubmitAttemptRequest;
import hu.kornel.server.application.usecase.assignments.ContinueAssignmentAttemptUseCase;
import hu.kornel.server.application.usecase.assignments.GetAttemptResultsUseCase;
import hu.kornel.server.application.usecase.assignments.GetGroupAssignmentsUseCase;
import hu.kornel.server.application.usecase.assignments.GetStudentAttemptsUseCase;
import hu.kornel.server.application.usecase.assignments.StartAssignmentAttemptUseCase;
import hu.kornel.server.application.usecase.assignments.SubmitAssignmentAttemptUseCase;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.exception.assignments.AssignmentNotFoundException;
import hu.kornel.server.infrastructure.security.UserPrincipal;
import hu.kornel.server.presentation.mapper.AssignmentMapper;
import hu.kornel.server.presentation.mapper.AttemptMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/groups/{groupId}/assignments")
@RequiredArgsConstructor
@Slf4j
public class StudentAssignmentController {

	private final GetGroupAssignmentsUseCase getGroupAssignmentsUseCase;
	private final StartAssignmentAttemptUseCase startAssignmentAttemptUseCase;
	private final SubmitAssignmentAttemptUseCase submitAssignmentAttemptUseCase;
	private final GetAttemptResultsUseCase getAttemptResultsUseCase;
	private final GetStudentAttemptsUseCase getStudentAttemptsUseCase;
	private final ContinueAssignmentAttemptUseCase continueAssignmentAttemptUseCase;
	private final AssignmentMapper assignmentMapper;
	private final AttemptMapper attemptMapper;

	@GetMapping
	public ResponseEntity<List<AssignmentSummaryResponse>> getGroupAssignments(
			@PathVariable Long groupId,
			@AuthenticationPrincipal UserPrincipal userPrincipal) {
		log.info("GET /api/groups/{}/assignments - Fetching assignments for group", groupId);

		List<Assignment> assignments = getGroupAssignmentsUseCase.execute(groupId, userPrincipal.getId());

		List<AssignmentSummaryResponse> response = assignments.stream()
				.map(assignment -> assignmentMapper.toSummaryResponseWithAttempts(assignment, userPrincipal.getId()))
				.collect(Collectors.toList());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/{assignmentId}/attempts")
	public ResponseEntity<AttemptResponse> startAttempt(
			@PathVariable Long groupId,
			@PathVariable Long assignmentId,
			@AuthenticationPrincipal UserPrincipal userPrincipal) {
		log.info("POST /api/groups/{}/assignments/{}/attempts - Starting attempt", groupId, assignmentId);

		AssignmentAttempt attempt = startAssignmentAttemptUseCase.execute(
				assignmentId, groupId, userPrincipal.getId());
		AttemptResponse response = attemptMapper.toResponse(attempt, false);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/{assignmentId}/attempts/{attemptId}/submit")
	public ResponseEntity<AttemptResponse> submitAttempt(
			@PathVariable Long groupId,
			@PathVariable Long assignmentId,
			@PathVariable Long attemptId,
			@Valid @RequestBody SubmitAttemptRequest request,
			@AuthenticationPrincipal UserPrincipal userPrincipal) {
		log.info("POST /api/groups/{}/assignments/{}/attempts/{}/submit - Submitting attempt", groupId, assignmentId, attemptId);

		AssignmentAttempt attempt = submitAssignmentAttemptUseCase.execute(attemptId, request.getAnswers(), userPrincipal.getId());
		AttemptResponse response = attemptMapper.toResponse(attempt, true);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{assignmentId}/attempts/{attemptId}")
	public ResponseEntity<AttemptResponse> getAttemptResults(
			@PathVariable Long groupId,
			@PathVariable Long assignmentId,
			@PathVariable Long attemptId,
			@AuthenticationPrincipal UserPrincipal userPrincipal) {
		log.info("GET /api/groups/{}/assignments/{}/attempts/{} - Fetching attempt results", groupId, assignmentId, attemptId);

		AssignmentAttempt attempt = getAttemptResultsUseCase.execute(attemptId, userPrincipal.getId());
		AttemptResponse response = attemptMapper.toResponse(attempt, true);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{assignmentId}/attempts")
	public ResponseEntity<List<AttemptSummaryResponse>> getStudentAttempts(
			@PathVariable Long groupId,
			@PathVariable Long assignmentId,
			@AuthenticationPrincipal UserPrincipal userPrincipal) {
		log.info("GET /api/groups/{}/assignments/{}/attempts - Fetching student attempts", groupId, assignmentId);

		List<AssignmentAttempt> attempts = getStudentAttemptsUseCase.execute(assignmentId, groupId, userPrincipal.getId());
		List<AttemptSummaryResponse> response = attemptMapper.toSummaryResponseList(attempts);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{assignmentId}")
	public ResponseEntity<AssignmentResponse> getAssignment(
			@PathVariable Long groupId,
			@PathVariable Long assignmentId,
			@AuthenticationPrincipal UserPrincipal userPrincipal) {

		Assignment assignment = getGroupAssignmentsUseCase.execute(groupId, userPrincipal.getId())
				.stream()
				.filter(a -> a.getId().equals(assignmentId))
				.findFirst()
				.orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

		AssignmentResponse response = assignmentMapper.toResponseWithAttempts(assignment, userPrincipal.getId());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{assignmentId}/attempts/active")
	public ResponseEntity<AttemptResponse> getActiveAttempt(
			@PathVariable Long groupId,
			@PathVariable Long assignmentId,
			@AuthenticationPrincipal UserPrincipal userPrincipal) {

		AssignmentAttempt attempt = continueAssignmentAttemptUseCase.execute(assignmentId, groupId, userPrincipal.getId());
		AttemptResponse response = attemptMapper.toResponse(attempt, false);

		return ResponseEntity.ok(response);
	}
}