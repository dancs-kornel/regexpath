package hu.kornel.server.domain.repository;

import java.util.List;

import hu.kornel.server.domain.entities.ExerciseAttempt;

public interface ExerciseAttemptRepositoryInterface {
    ExerciseAttempt save(ExerciseAttempt attempt);
    List<ExerciseAttempt> findRecentByUserId(Long userId, int limit);
    List<ExerciseAttempt> findByUserIdAndExerciseId(Long userId, String lessonId, String exerciseId);
    int countAttemptsByUserIdAndExerciseId(Long userId, String lessonId, String exerciseId);
    void deleteByUserId(Long userId);
}