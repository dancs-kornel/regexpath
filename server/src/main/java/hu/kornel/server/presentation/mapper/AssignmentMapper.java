package hu.kornel.server.presentation.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import hu.kornel.server.application.dto.assignments.AssignmentResponse;
import hu.kornel.server.application.dto.assignments.AssignmentSummaryResponse;
import hu.kornel.server.application.dto.assignments.ExerciseResponse;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AssignmentMapper {
    private final AssignmentAttemptRepositoryInterface attemptRepository;

    
    public AssignmentMapper(AssignmentAttemptRepositoryInterface attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    
    public AssignmentSummaryResponse toSummaryResponseWithAttempts(Assignment assignment, Long studentId) {
        List<AssignmentAttempt> attempts = attemptRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), studentId);

        boolean hasActiveAttempt = attempts.stream()
                .anyMatch(a -> !a.isCompleted() && !a.isExpired());
        log.debug("Attempts for assignment " + assignment.getId() + ": " + attempts.size());
        log.debug("Has active attempt: " + hasActiveAttempt);

        Integer bestScore = attempts.stream()
                .filter(AssignmentAttempt::isCompleted)
                .map(AssignmentAttempt::getScore)
                .max(Integer::compareTo)
                .orElse(null);

        boolean isExpired = assignment.getDueDate() != null &&
                assignment.getDueDate().isBefore(LocalDateTime.now());

        return AssignmentSummaryResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .status(assignment.getStatus())
                .dueDate(assignment.getDueDate())
                .totalPoints(assignment.getTotalPoints())
                .exerciseCount(assignment.getExerciseCount())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .attemptsUsed(attempts.size())
                .hasActiveAttempt(hasActiveAttempt)
                .isExpired(isExpired)
                .bestScore(bestScore)
                .attemptLimit(assignment.getMaxAttempts())
                .build();
    }

    public AssignmentResponse toResponse(Assignment assignment) {
        List<ExerciseResponse> exerciseResponses = null;
        if (assignment.getExercises() != null) {
            exerciseResponses = assignment.getExercises().stream()
                    .map(this::toExerciseResponse)
                    .collect(Collectors.toList());
        }

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .teacherId(assignment.getTeacherId())
                .status(assignment.getStatus())
                .dueDate(assignment.getDueDate())
                .timeLimitMinutes(assignment.getTimeLimitMinutes())
                .maxAttempts(assignment.getMaxAttempts())
                .totalPoints(assignment.getTotalPoints())
                .exerciseCount(assignment.getExerciseCount())
                .exercises(exerciseResponses)
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    public AssignmentSummaryResponse toSummaryResponse(Assignment assignment) {
        return AssignmentSummaryResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .status(assignment.getStatus())
                .dueDate(assignment.getDueDate())
                .totalPoints(assignment.getTotalPoints())
                .exerciseCount(assignment.getExerciseCount())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    public List<AssignmentSummaryResponse> toSummaryResponseList(List<Assignment> assignments) {
        return assignments.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public ExerciseResponse toExerciseResponse(Exercise exercise) {
        return ExerciseResponse.builder()
                .id(exercise.getId())
                .assignmentId(exercise.getAssignmentId())
                .type(exercise.getType())
                .orderIndex(exercise.getOrderIndex())
                .points(exercise.getPoints())
                .title(exercise.getTitle())
                .question(exercise.getQuestion())
                .configJson(exercise.getConfigJson())
                .explanation(exercise.getExplanation())
                .build();
    }

    public AssignmentResponse toResponseWithAttempts(Assignment assignment, Long studentId) {
        List<AssignmentAttempt> attempts = attemptRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), studentId);

        boolean hasActiveAttempt = attempts.stream()
                .anyMatch(a -> !a.isCompleted() && !a.isExpired());

        Integer bestScore = attempts.stream()
                .filter(AssignmentAttempt::isCompleted)
                .map(AssignmentAttempt::getScore)
                .max(Integer::compareTo)
                .orElse(null);

        boolean isExpired = assignment.getDueDate() != null &&
                assignment.getDueDate().isBefore(LocalDateTime.now());

        List<ExerciseResponse> exerciseResponses = null;
        if (assignment.getExercises() != null) {
            exerciseResponses = assignment.getExercises().stream()
                    .map(this::toExerciseResponse)
                    .collect(Collectors.toList());
        }

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .teacherId(assignment.getTeacherId())
                .status(assignment.getStatus())
                .dueDate(assignment.getDueDate())
                .timeLimitMinutes(assignment.getTimeLimitMinutes())
                .maxAttempts(assignment.getMaxAttempts())
                .totalPoints(assignment.getTotalPoints())
                .exerciseCount(assignment.getExerciseCount())
                .exercises(exerciseResponses)
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .attemptsUsed(attempts.size())
                .hasActiveAttempt(hasActiveAttempt)
                .isExpired(isExpired)
                .bestScore(bestScore)
                .attemptLimit(assignment.getMaxAttempts())
                .build();
    }
}