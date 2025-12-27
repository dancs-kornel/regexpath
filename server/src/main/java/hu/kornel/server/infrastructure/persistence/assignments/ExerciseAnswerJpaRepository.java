package hu.kornel.server.infrastructure.persistence.assignments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseAnswerJpaRepository extends JpaRepository<ExerciseAnswerJpaEntity, Long> {
    
    List<ExerciseAnswerJpaEntity> findByAttemptId(Long attemptId);
    
    @Modifying
    @Query("DELETE FROM ExerciseAnswerJpaEntity e WHERE e.attemptId = :attemptId")
    void deleteByAttemptId(@Param("attemptId") Long attemptId);
}