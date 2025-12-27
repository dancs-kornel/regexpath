package hu.kornel.server.application.dto.assignments;

import hu.kornel.server.domain.entities.assignments.ExerciseType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExerciseRequest {
    
    @NotNull(message = "A feladat típusa kötelező")
    private ExerciseType type;
    
    @NotNull(message = "A sorrend megadása kötelező")
    @Min(value = 0, message = "A sorrend nem lehet negatív")
    private Integer orderIndex;
    
    @NotNull(message = "A pontszám megadása kötelező")
    @Min(value = 1, message = "A pontszám legalább 1 kell legyen")
    private Integer points;
    
    @NotBlank(message = "A cím megadása kötelező")
    @Size(max = 200, message = "A cím maximum 200 karakter lehet")
    private String title;
    
    @NotBlank(message = "A kérdés megadása kötelező")
    private String question;
    
    @NotBlank(message = "A konfiguráció megadása kötelező")
    private String configJson;
    
    private String explanation;
}