package hu.kornel.server.domain.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseAttempt {
    private Long id;
    private Long userId;
    private String lessonId;
    private String exerciseId;
    private Integer attemptNumber;
    private boolean correct;
    private LocalDateTime attemptedAt;

    public boolean isFirstAttempt() {
        return attemptNumber != null && attemptNumber == 1;
    }

    public boolean isCorrectFirstAttempt() {
        return isFirstAttempt() && correct;
    }

    public boolean isMultipleAttempts() {
        return attemptNumber != null && attemptNumber > 1;
    }
}