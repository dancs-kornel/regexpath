package hu.kornel.server.application.dto.assignments;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAttemptViewResponse {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    
    private Long assignmentId;
    private String assignmentTitle;
    private Integer totalPoints;
    
    private Long attemptId;
    private Integer attemptNumber;
    private Integer score;
    private Integer maxScore;
    private Double percentageScore;
    
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Long timeSpentMinutes;
    
    private boolean completed;
    private boolean expired;
    
    private List<TeacherExerciseAnswerView> answers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherExerciseAnswerView {
        private Long answerId;
        private Long exerciseId;
        private String exerciseTitle;
        private String exerciseQuestion;
        private String exerciseType;
        
        private String studentAnswerJson;
        private Boolean correct;
        private Integer pointsEarned;
        private Integer pointsPossible;
        
        private String validationResultJson;
        
        private String exerciseConfigJson;
    }
}