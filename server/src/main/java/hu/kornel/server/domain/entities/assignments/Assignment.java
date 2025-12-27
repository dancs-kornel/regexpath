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
public class Assignment {
    private Long id;
    private String title;
    private String description;
    private Long teacherId;
    
    private AssignmentStatus status;
    
    private LocalDateTime dueDate;
    private Integer timeLimitMinutes; 
    private Integer maxAttempts;      
    
    @Builder.Default
    private List<Exercise> exercises = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    
    public boolean isDraft() {
        return status == AssignmentStatus.DRAFT;
    }
    
    public boolean isSubmitted() {
        return status == AssignmentStatus.SUBMITTED;
    }
    
    public void submit() {
        if (isSubmitted()) {
            throw new IllegalStateException("Assignment is already submitted");
        }
        this.status = AssignmentStatus.SUBMITTED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void revertToDraft() {
        if (isDraft()) {
            throw new IllegalStateException("Assignment is already in draft status");
        }
        this.status = AssignmentStatus.DRAFT;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isOwnedBy(Long userId) {
        return this.teacherId.equals(userId);
    }
    
    public boolean canBeEdited() {
        return isDraft();
    }
    
    public void addExercise(Exercise exercise) {
        this.exercises.add(exercise);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void removeExercise(Long exerciseId) {
        this.exercises.removeIf(e -> e.getId().equals(exerciseId));
        this.updatedAt = LocalDateTime.now();
    }
    
    public int getTotalPoints() {
        return exercises.stream()
                .mapToInt(Exercise::getPoints)
                .sum();
    }
    
    public int getExerciseCount() {
        return exercises.size();
    }
    
    public boolean isExpired() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }
    
    public boolean hasTimeLimit() {
        return timeLimitMinutes != null && timeLimitMinutes > 0;
    }
    
    public boolean hasUnlimitedAttempts() {
        return maxAttempts == null || maxAttempts <= 0;
    }
}