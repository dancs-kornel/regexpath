package hu.kornel.server.domain.repository.assignments;

import java.util.List;
import java.util.Optional;

import hu.kornel.server.domain.entities.assignments.ExerciseAnswer;

public interface ExerciseAnswerRepositoryInterface {
    Optional<ExerciseAnswer> findById(Long id);
    List<ExerciseAnswer> findByAttemptId(Long attemptId);
    ExerciseAnswer save(ExerciseAnswer answer);
    void deleteById(Long id);
    void deleteByAttemptId(Long attemptId);
}