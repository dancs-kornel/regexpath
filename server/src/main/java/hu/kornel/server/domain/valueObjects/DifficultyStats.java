package hu.kornel.server.domain.valueObjects;

import hu.kornel.server.domain.entities.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DifficultyStats {
    private int consecutiveCorrectFirstAttempts;
    private int consecutiveStrugglingExercises;
    private int currentExerciseAttempts;
    private DifficultyLevel currentLevel;

    private static final int INCREASE_THRESHOLD = 3;
    private static final int DECREASE_THRESHOLD = 2;
    private static final int STRUGGLING_ATTEMPTS = 3;

    public boolean shouldPromptIncrease() {
        return currentLevel != null && currentLevel.canIncrease() && consecutiveCorrectFirstAttempts >= INCREASE_THRESHOLD;
    }

    public boolean shouldPromptDecrease() {
        return currentLevel != null && currentLevel.canDecrease() && consecutiveStrugglingExercises >= STRUGGLING_ATTEMPTS;
    }

    public boolean isStruggling() { return currentExerciseAttempts >= STRUGGLING_ATTEMPTS; }

    public boolean hasPromptCondition() { return shouldPromptIncrease() || shouldPromptDecrease(); }
}
