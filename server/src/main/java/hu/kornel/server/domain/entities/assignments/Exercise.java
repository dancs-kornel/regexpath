package hu.kornel.server.domain.entities.assignments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exercise {
    private Long id;
    private Long assignmentId;
    
    private ExerciseType type;
    private Integer orderIndex;     
    private Integer points;         
    
    private String title;
    private String question;
    
    private String configJson;
    
    private String explanation;
    
    public boolean isMultipleChoice() {
        return type == ExerciseType.MULTIPLE_CHOICE;
    }
    
    public boolean isRadio() {
        return type == ExerciseType.RADIO;
    }
    
    public boolean isRegexSandbox() {
        return type == ExerciseType.REGEX_SANDBOX;
    }
    
    public boolean isXPathSandbox() {
        return type == ExerciseType.XPATH_SANDBOX;
    }
}