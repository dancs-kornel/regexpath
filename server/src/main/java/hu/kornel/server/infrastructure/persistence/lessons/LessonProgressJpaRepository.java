package hu.kornel.server.infrastructure.persistence.lessons;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonProgressJpaRepository extends JpaRepository<LessonProgressJpaEntity, Long> {
    
    Optional<LessonProgressJpaEntity> findByUserIdAndLessonId(Long userId, String lessonId);
    
    List<LessonProgressJpaEntity> findAllByUserId(Long userId);
    
    List<LessonProgressJpaEntity> findAllByUserIdAndCompletedTrue(Long userId);
    
    @Query("SELECT lp FROM LessonProgressJpaEntity lp WHERE lp.userId = :userId ORDER BY lp.lastAccessedAt DESC LIMIT 1")
    Optional<LessonProgressJpaEntity> findLastAccessedByUserId(@Param("userId") Long userId);
    
    boolean existsByUserIdAndLessonId(Long userId, String lessonId);
    
    void deleteByUserIdAndLessonId(Long userId, String lessonId);
}