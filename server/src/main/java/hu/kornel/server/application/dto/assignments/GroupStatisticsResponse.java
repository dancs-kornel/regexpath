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
public class GroupStatisticsResponse {
    private Long groupId;
    private String groupName;
    private Integer totalStudents;
    
    private Integer totalAssignments;
    private Double overallCompletionRate;   
    
    private List<AssignmentSummaryRow> assignmentSummaries;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentSummaryRow {
        private Long assignmentId;
        private String assignmentTitle;
        private LocalDateTime dueDate;
        private Integer totalPoints;
        
        private Integer studentsCompleted;
        private Integer studentsInProgress;
        private Integer studentsNotStarted;
        
        private Double averageScore;
        private Double completionRate;
    }
}