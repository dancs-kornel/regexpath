package hu.kornel.server.application.dto.assignments;

import java.time.LocalDateTime;
import java.util.List;

import hu.kornel.server.domain.entities.assignments.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private Long id;
    private String title;
    private String description;
    private Long teacherId;
    private AssignmentStatus status;
    private LocalDateTime dueDate;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private Integer totalPoints;
    private Integer exerciseCount;
    private List<ExerciseResponse> exercises;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer attemptsUsed;
    private Boolean hasActiveAttempt;
    private Boolean isExpired;
    private Integer bestScore;
    private Integer attemptLimit;
}