package hu.kornel.server.application.dto.difficulty;

import hu.kornel.server.domain.entities.DifficultyLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDifficultyRequest {
    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;
}