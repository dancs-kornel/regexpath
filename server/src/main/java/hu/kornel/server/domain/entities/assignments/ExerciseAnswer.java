package hu.kornel.server.domain.entities.assignments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseAnswer {
    private Long id;
    private Long attemptId;
    private Long exerciseId;
    
    private ExerciseType exerciseType;
    
    private String answerJson;
    
    private Boolean correct;
    private Integer pointsEarned;
    private Integer pointsPossible;
    
    private String validationResultJson;
    
    public boolean isCorrect() {
        return correct != null && correct;
    }
}