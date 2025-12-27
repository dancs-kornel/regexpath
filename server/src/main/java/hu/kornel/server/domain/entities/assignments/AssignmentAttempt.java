package hu.kornel.server.domain.entities.assignments;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentAttempt {
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private Long groupId;           
    
    private Integer attemptNumber;   
    private Integer score;           
    private Integer maxScore;        
    
    @Builder.Default
    private List<ExerciseAnswer> answers = new ArrayList<>();
    
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime expiresAt; 
    
    private boolean completed;
    
    
    public boolean isCompleted() {
        return completed;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isInProgress() {
        return !completed && !isExpired();
    }
    
    public void submit(int calculatedScore) {
        if (completed) {
            throw new IllegalStateException("Attempt is already submitted");
        }
        if (isExpired()) {
            throw new IllegalStateException("Attempt has expired");
        }
        this.score = calculatedScore;
        this.submittedAt = LocalDateTime.now();
        this.completed = true;
    }
    
    public void addAnswer(ExerciseAnswer answer) {
        this.answers.add(answer);
    }
    
    public double getPercentageScore() {
        if (maxScore == null || maxScore == 0 || score == null) {
            return 0.0;
        }
        return (score * 100.0) / maxScore;
    }
    
    public long getTimeSpentMinutes() {
        if (startedAt == null) {
            return 0;
        }
        LocalDateTime endTime = submittedAt != null ? submittedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, endTime).toMinutes();
    }
}