package hu.kornel.server.application.dto.assignments;

import hu.kornel.server.domain.entities.assignments.ExerciseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseAnswerResponse {
    private Long id;
    private Long exerciseId;
    private ExerciseType exerciseType;
    private String answerJson;
    private Boolean correct;
    private Integer pointsEarned;
    private Integer pointsPossible;
    private String validationResultJson; 
}