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
public class StudentProgressResponse {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    
    private Long groupId;
    private String groupName;
    
    private Integer totalAssignments;
    private Integer assignmentsCompleted;
    private Integer assignmentsInProgress;
    private Integer assignmentsNotStarted;
    
    private Integer totalPointsEarned;      
    private Integer totalPointsPossible;    
    private Double completionRate;
    
    private List<AssignmentProgressRow> assignmentProgress;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentProgressRow {
        private Long assignmentId;
        private String assignmentTitle;
        private LocalDateTime dueDate;
        private Integer totalPoints;
        
        private String status;             
        private Integer attemptsUsed;
        private Integer maxAttempts;
        private Integer bestScore;
        private Double percentageScore;
        
        private LocalDateTime firstStartedAt;
        private LocalDateTime lastSubmittedAt;
        
        private Long bestAttemptId;
    }
}