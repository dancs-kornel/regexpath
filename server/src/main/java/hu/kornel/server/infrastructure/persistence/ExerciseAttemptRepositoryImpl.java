package hu.kornel.server.infrastructure.persistence;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.ExerciseAttempt;
import hu.kornel.server.domain.repository.ExerciseAttemptRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ExerciseAttemptRepositoryImpl implements ExerciseAttemptRepositoryInterface {
    
    private final ExerciseAttemptJpaRepository jpaRepository;
    
    @Override
    public ExerciseAttempt save(ExerciseAttempt attempt) {
        ExerciseAttemptJpaEntity jpaEntity = ExerciseAttemptJpaEntity.fromDomain(attempt);
        ExerciseAttemptJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    public List<ExerciseAttempt> findRecentByUserId(Long userId, int limit) {
        return jpaRepository.findRecentByUserId(userId, limit).stream()
                .map(ExerciseAttemptJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExerciseAttempt> findByUserIdAndExerciseId(Long userId, String lessonId, String exerciseId) {
        return jpaRepository.findByUserIdAndLessonIdAndExerciseIdOrderByAttemptedAtDesc(userId, lessonId, exerciseId)
                .stream()
                .map(ExerciseAttemptJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public int countAttemptsByUserIdAndExerciseId(Long userId, String lessonId, String exerciseId) {
        return jpaRepository.countByUserIdAndLessonIdAndExerciseId(userId, lessonId, exerciseId);
    }
    
    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }
}