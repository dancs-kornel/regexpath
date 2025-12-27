package hu.kornel.server.infrastructure.persistence.assignments;

import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.entities.assignments.ExerciseType;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercises")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private AssignmentJpaEntity assignment;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExerciseType type;
    
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
    
    @Column(nullable = false)
    private Integer points;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
    private String configJson;
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    
    public static ExerciseJpaEntity fromDomain(Exercise exercise) {
        return ExerciseJpaEntity.builder()
                .id(exercise.getId())
                .assignmentId(exercise.getAssignmentId())
                .type(exercise.getType())
                .orderIndex(exercise.getOrderIndex())
                .points(exercise.getPoints())
                .title(exercise.getTitle())
                .question(exercise.getQuestion())
                .configJson(exercise.getConfigJson())
                .explanation(exercise.getExplanation())
                .build();
    }
    
    public Exercise toDomain() {
        return Exercise.builder()
                .id(this.id)
                .assignmentId(this.assignmentId)
                .type(this.type)
                .orderIndex(this.orderIndex)
                .points(this.points)
                .title(this.title)
                .question(this.question)
                .configJson(this.configJson)
                .explanation(this.explanation)
                .build();
    }
}