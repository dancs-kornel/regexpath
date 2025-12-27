package hu.kornel.server.application.dto.difficulty;

import hu.kornel.server.domain.entities.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifficultyPromptResponse {
    private boolean shouldPrompt;
    private String promptType; 
    private DifficultyLevel currentLevel;
    private DifficultyLevel suggestedLevel;
    private String message;
    private int consecutiveCorrectFirstAttempts;
    private int consecutiveStrugglingExercises;
}