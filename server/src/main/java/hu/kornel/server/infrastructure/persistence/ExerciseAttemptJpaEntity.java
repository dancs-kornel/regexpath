package hu.kornel.server.infrastructure.persistence;

import java.time.LocalDateTime;

import hu.kornel.server.domain.entities.ExerciseAttempt;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercise_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseAttemptJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "lesson_id", nullable = false, length = 100)
    private String lessonId;
    
    @Column(name = "exercise_id", nullable = false, length = 100)
    private String exerciseId;
    
    @Column(name = "attempt_number", nullable = false)
    @Builder.Default
    private Integer attemptNumber = 1;
    
    @Column(name = "is_correct", nullable = false)
    private boolean correct;
    
    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;
    
    @PrePersist
    protected void onCreate() {
        if (attemptedAt == null) {
            attemptedAt = LocalDateTime.now();
        }
        if (attemptNumber == null) {
            attemptNumber = 1;
        }
    }
    
    public ExerciseAttempt toDomain() {
        return ExerciseAttempt.builder()
                .id(this.id)
                .userId(this.userId)
                .lessonId(this.lessonId)
                .exerciseId(this.exerciseId)
                .attemptNumber(this.attemptNumber)
                .correct(this.correct)
                .attemptedAt(this.attemptedAt)
                .build();
    }
    
    public static ExerciseAttemptJpaEntity fromDomain(ExerciseAttempt attempt) {
        ExerciseAttemptJpaEntity entity = ExerciseAttemptJpaEntity.builder()
                .userId(attempt.getUserId())
                .lessonId(attempt.getLessonId())
                .exerciseId(attempt.getExerciseId())
                .attemptNumber(attempt.getAttemptNumber())
                .correct(attempt.isCorrect())
                .attemptedAt(attempt.getAttemptedAt())
                .build();
        
        if (attempt.getId() != null) {
            entity.setId(attempt.getId());
        }
        
        return entity;
    }
}