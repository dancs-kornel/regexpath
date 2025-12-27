package hu.kornel.server.infrastructure.persistence.assignments;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentStatus;
import hu.kornel.server.infrastructure.persistence.UserJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private UserJpaEntity teacher;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.DRAFT;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;
    
    @Column(name = "max_attempts")
    private Integer maxAttempts;
    
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExerciseJpaEntity> exercises = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    
    public static AssignmentJpaEntity fromDomain(Assignment assignment) {
        AssignmentJpaEntity entity = AssignmentJpaEntity.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .teacherId(assignment.getTeacherId())
                .status(assignment.getStatus())
                .dueDate(assignment.getDueDate())
                .timeLimitMinutes(assignment.getTimeLimitMinutes())
                .maxAttempts(assignment.getMaxAttempts())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
        
        
        if (assignment.getExercises() != null && !assignment.getExercises().isEmpty()) {
            List<ExerciseJpaEntity> exerciseEntities = assignment.getExercises().stream()
                    .map(ex -> {
                        ExerciseJpaEntity exerciseEntity = ExerciseJpaEntity.fromDomain(ex);
                        exerciseEntity.setAssignment(entity);
                        return exerciseEntity;
                    })
                    .collect(Collectors.toList());
            entity.setExercises(exerciseEntities);
        }
        
        return entity;
    }
    
    public Assignment toDomain() {
        Assignment assignment = Assignment.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .teacherId(this.teacherId)
                .status(this.status)
                .dueDate(this.dueDate)
                .timeLimitMinutes(this.timeLimitMinutes)
                .maxAttempts(this.maxAttempts)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
        
        
        if (this.exercises != null && !this.exercises.isEmpty()) {
            assignment.setExercises(
                this.exercises.stream()
                    .map(ExerciseJpaEntity::toDomain)
                    .collect(Collectors.toList())
            );
        }
        
        return assignment;
    }
}