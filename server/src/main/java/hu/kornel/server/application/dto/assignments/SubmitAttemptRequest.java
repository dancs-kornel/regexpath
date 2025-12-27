package hu.kornel.server.application.dto.assignments;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAttemptRequest {
    
    @NotEmpty(message = "Legalább egy válasz megadása kötelező")
    private List<@Valid ExerciseAnswerRequest> answers;
}