package hu.kornel.server.application.dto.assignments;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptResponse {
    private Long id;
    private Long assignmentId;
    private Integer attemptNumber;
    private Integer score;
    private Integer maxScore;
    private Double percentageScore;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime expiresAt;
    private boolean completed;
    private boolean expired;
    private boolean canViewAnswers;  
    private List<ExerciseAnswerResponse> answers;
}