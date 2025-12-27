package hu.kornel.server.infrastructure.persistence.assignments;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;

import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.infrastructure.persistence.GroupJpaEntity;
import hu.kornel.server.infrastructure.persistence.UserJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "assignment_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentAttemptJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private AssignmentJpaEntity assignment;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private UserJpaEntity student;
    
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private GroupJpaEntity group;
    
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;
    
    @Column
    private Integer score;
    
    @Column(name = "max_score", nullable = false)
    private Integer maxScore;
    
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExerciseAnswerJpaEntity> answers = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;
    
    
    public static AssignmentAttemptJpaEntity fromDomain(AssignmentAttempt attempt) {
        AssignmentAttemptJpaEntity entity = AssignmentAttemptJpaEntity.builder()
                .id(attempt.getId())
                .assignmentId(attempt.getAssignmentId())
                .studentId(attempt.getStudentId())
                .groupId(attempt.getGroupId())
                .attemptNumber(attempt.getAttemptNumber())
                .score(attempt.getScore())
                .maxScore(attempt.getMaxScore())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .expiresAt(attempt.getExpiresAt())
                .completed(attempt.isCompleted())
                .build();
        
        
        if (attempt.getAnswers() != null && !attempt.getAnswers().isEmpty()) {
            List<ExerciseAnswerJpaEntity> answerEntities = attempt.getAnswers().stream()
                    .map(ans -> {
                        ExerciseAnswerJpaEntity answerEntity = ExerciseAnswerJpaEntity.fromDomain(ans);
                        answerEntity.setAttempt(entity);
                        return answerEntity;
                    })
                    .collect(Collectors.toList());
            entity.setAnswers(answerEntities);
        }
        
        return entity;
    }
    
    public AssignmentAttempt toDomain() {
        AssignmentAttempt attempt = AssignmentAttempt.builder()
                .id(this.id)
                .assignmentId(this.assignmentId)
                .studentId(this.studentId)
                .groupId(this.groupId)
                .attemptNumber(this.attemptNumber)
                .score(this.score)
                .maxScore(this.maxScore)
                .startedAt(this.startedAt)
                .submittedAt(this.submittedAt)
                .expiresAt(this.expiresAt)
                .completed(this.completed)
                .build();
        
        
        if (this.answers != null && !this.answers.isEmpty()) {
            attempt.setAnswers(
                this.answers.stream()
                    .map(ExerciseAnswerJpaEntity::toDomain)
                    .collect(Collectors.toList())
            );
        }
        
        return attempt;
    }
}