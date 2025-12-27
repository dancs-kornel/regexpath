package hu.kornel.server.application.dto.assignments;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequest {
    
    @NotBlank(message = "A cím megadása kötelező")
    @Size(max = 200, message = "A cím maximum 200 karakter lehet")
    private String title;
    
    @Size(max = 5000, message = "A leírás maximum 5000 karakter lehet")
    private String description;
    
    private LocalDateTime dueDate;
    
    private Integer timeLimitMinutes;
    
    private Integer maxAttempts;

    List<AddExerciseRequest> exercises;
}