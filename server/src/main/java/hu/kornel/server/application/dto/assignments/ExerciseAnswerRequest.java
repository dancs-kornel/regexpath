package hu.kornel.server.application.dto.assignments;

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
public class ExerciseAnswerRequest {
    
    @NotNull(message = "A feladat azonosítója kötelező")
    private Long exerciseId;
    
    @NotBlank(message = "A válasz megadása kötelező")
    private String answerJson; 
}