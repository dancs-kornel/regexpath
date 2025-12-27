package hu.kornel.server.infrastructure.persistence.assignments;

import hu.kornel.server.domain.entities.assignments.ExerciseAnswer;
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
@Table(name = "exercise_answers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseAnswerJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", insertable = false, updatable = false)
    private AssignmentAttemptJpaEntity attempt;
    
    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", insertable = false, updatable = false)
    private ExerciseJpaEntity exercise;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, length = 20)
    private ExerciseType exerciseType;
    
    @Column(name = "answer_json", nullable = false, columnDefinition = "TEXT")
    private String answerJson;
    
    @Column(nullable = false)
    private boolean correct;
    
    @Column(name = "points_earned")
    private Integer pointsEarned;
    
    @Column(name = "points_possible", nullable = false)
    private Integer pointsPossible;
    
    @Column(name = "validation_result_json", columnDefinition = "TEXT")
    private String validationResultJson;
    
    
    public static ExerciseAnswerJpaEntity fromDomain(ExerciseAnswer answer) {
        return ExerciseAnswerJpaEntity.builder()
                .id(answer.getId())
                .attemptId(answer.getAttemptId())
                .exerciseId(answer.getExerciseId())
                .exerciseType(answer.getExerciseType())
                .answerJson(answer.getAnswerJson())
                .correct(answer.isCorrect())
                .pointsEarned(answer.getPointsEarned())
                .pointsPossible(answer.getPointsPossible())
                .validationResultJson(answer.getValidationResultJson())
                .build();
    }
    
    public ExerciseAnswer toDomain() {
        return ExerciseAnswer.builder()
                .id(this.id)
                .attemptId(this.attemptId)
                .exerciseId(this.exerciseId)
                .exerciseType(this.exerciseType)
                .answerJson(this.answerJson)
                .correct(this.correct)
                .pointsEarned(this.pointsEarned)
                .pointsPossible(this.pointsPossible)
                .validationResultJson(this.validationResultJson)
                .build();
    }
}