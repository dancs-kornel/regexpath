package hu.kornel.server.infrastructure.persistence.lessons;

import java.time.LocalDateTime;

import hu.kornel.server.domain.entities.LessonProgress;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "lesson_progress",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_lesson", columnNames = {"user_id", "lesson_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "lesson_id", nullable = false, length = 100)
    private String lessonId;
    
    @Column(name = "is_completed", nullable = false)
    private boolean completed;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastAccessedAt == null) {
            lastAccessedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public LessonProgress toDomain() {
        return LessonProgress.builder()
                .id(this.id)
                .userId(this.userId)
                .lessonId(this.lessonId)
                .completed(this.completed)
                .completedAt(this.completedAt)
                .lastAccessedAt(this.lastAccessedAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
    
    public static LessonProgressJpaEntity fromDomain(LessonProgress lessonProgress) {
        LessonProgressJpaEntity entity = LessonProgressJpaEntity.builder()
                .userId(lessonProgress.getUserId())
                .lessonId(lessonProgress.getLessonId())
                .completed(lessonProgress.isCompleted())
                .completedAt(lessonProgress.getCompletedAt())
                .lastAccessedAt(lessonProgress.getLastAccessedAt())
                .createdAt(lessonProgress.getCreatedAt())
                .updatedAt(lessonProgress.getUpdatedAt())
                .build();
        
        if (lessonProgress.getId() != null) {
            entity.setId(lessonProgress.getId());
        }
        
        return entity;
    }
}