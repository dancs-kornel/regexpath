package hu.kornel.server.application.dto.difficulty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordExerciseAttemptRequest {
    @NotBlank(message = "Lesson ID is required")
    private String lessonId;
    
    @NotBlank(message = "Exercise ID is required")
    private String exerciseId;
    
    @NotNull(message = "Correct flag is required")
    private Boolean isCorrect;
    
    @NotNull(message = "Attempt number is required")
    @Min(value = 1, message = "Attempt number must be at least 1")
    private Integer attemptNumber;
}