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
public class AssignmentStatisticsResponse {
    private Long assignmentId;
    private String assignmentTitle;
    private Integer totalPoints;
    private Integer maxAttempts;
    private LocalDateTime dueDate;
    
    private Integer totalStudents;         
    private Integer studentsNotStarted;     
    private Integer studentsInProgress;     
    private Integer studentsCompleted;      
    
    private Double averageScore;            
    private Double completionRate;           
    
    private List<StudentStatisticsRow> studentStats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentStatisticsRow {
        private Long studentId;
        private String studentName;
        private String studentEmail;
        
        private String status;             
        private Integer attemptsUsed;
        private Integer bestScore;
        private Double percentageScore;
        
        private LocalDateTime firstStartedAt;
        private LocalDateTime lastSubmittedAt;
        
        private Long bestAttemptId;
    }
}