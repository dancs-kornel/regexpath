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
public class ExerciseResponse {
    private Long id;
    private Long assignmentId;
    private ExerciseType type;
    private Integer orderIndex;
    private Integer points;
    private String title;
    private String question;
    private String configJson;
    private String explanation;
}