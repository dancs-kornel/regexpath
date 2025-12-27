package hu.kornel.server.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseAttemptJpaRepository extends JpaRepository<ExerciseAttemptJpaEntity, Long> {
    
    @Query("SELECT ea FROM ExerciseAttemptJpaEntity ea WHERE ea.userId = :userId ORDER BY ea.attemptedAt DESC LIMIT :limit")
    List<ExerciseAttemptJpaEntity> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    
    List<ExerciseAttemptJpaEntity> findByUserIdAndLessonIdAndExerciseIdOrderByAttemptedAtDesc(
            Long userId, String lessonId, String exerciseId);
    
    int countByUserIdAndLessonIdAndExerciseId(Long userId, String lessonId, String exerciseId);
    
    void deleteByUserId(Long userId);
}