package hu.kornel.server.presentation.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import hu.kornel.server.application.dto.assignments.AttemptResponse;
import hu.kornel.server.application.dto.assignments.AttemptSummaryResponse;
import hu.kornel.server.application.dto.assignments.ExerciseAnswerResponse;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.entities.assignments.ExerciseAnswer;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttemptMapper {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    private final AssignmentAttemptRepositoryInterface attemptRepository;
    
    public AttemptResponse toResponse(AssignmentAttempt attempt, boolean includeAnswers) {
        
        boolean canViewAnswers = canViewAnswers(attempt);
        
        List<ExerciseAnswerResponse> answerResponses = List.of();
        if (includeAnswers && attempt.getAnswers() != null) {
            answerResponses = attempt.getAnswers().stream()
                    .map(answer -> toAnswerResponse(answer, canViewAnswers))
                    .collect(Collectors.toList());
        }
        
        return AttemptResponse.builder()
                .id(attempt.getId())
                .assignmentId(attempt.getAssignmentId())
                .attemptNumber(attempt.getAttemptNumber())
                .score(attempt.getScore())
                .maxScore(attempt.getMaxScore())
                .percentageScore(attempt.getPercentageScore())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .expiresAt(attempt.getExpiresAt())
                .completed(attempt.isCompleted())
                .expired(attempt.isExpired())
                .canViewAnswers(canViewAnswers)
                .answers(answerResponses)
                .build();
    }
    
    public AttemptSummaryResponse toSummaryResponse(AssignmentAttempt attempt) {
        return AttemptSummaryResponse.builder()
                .id(attempt.getId())
                .assignmentId(attempt.getAssignmentId())
                .attemptNumber(attempt.getAttemptNumber())
                .score(attempt.getScore())
                .maxScore(attempt.getMaxScore())
                .percentageScore(attempt.getPercentageScore())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .completed(attempt.isCompleted())
                .expired(attempt.isExpired())
                .build();
    }
    
    public List<AttemptSummaryResponse> toSummaryResponseList(List<AssignmentAttempt> attempts) {
        return attempts.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }
    
    private ExerciseAnswerResponse toAnswerResponse(ExerciseAnswer answer, boolean includeDetails) {
        return ExerciseAnswerResponse.builder()
                .id(answer.getId())
                .exerciseId(answer.getExerciseId())
                .exerciseType(answer.getExerciseType())
                .answerJson(answer.getAnswerJson())
                .correct(includeDetails && answer.getCorrect() != null ? answer.getCorrect() : null) 
                .pointsEarned(answer.getPointsEarned())
                .pointsPossible(answer.getPointsPossible())
                .validationResultJson(includeDetails ? answer.getValidationResultJson() : null)
                .build();
    }
    
    
    private boolean canViewAnswers(AssignmentAttempt attempt) {
        
        if (!attempt.isCompleted()) {
            return false;
        }
        
        
        if (attempt.isExpired()) {
            return true;
        }
        
        
        Assignment assignment = assignmentRepository.findById(attempt.getAssignmentId())
                .orElse(null);
        
        if (assignment == null) {
            return false;
        }
        
        
        if (assignment.hasUnlimitedAttempts()) {
            return false;
        }
        
        
        int attemptCount = attemptRepository.countByAssignmentIdAndStudentId(
                attempt.getAssignmentId(), attempt.getStudentId());
        
        return attemptCount >= assignment.getMaxAttempts();
    }
}