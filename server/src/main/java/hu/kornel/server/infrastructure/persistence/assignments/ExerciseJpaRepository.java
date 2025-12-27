package hu.kornel.server.infrastructure.persistence.assignments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseJpaRepository extends JpaRepository<ExerciseJpaEntity, Long> {
    
    List<ExerciseJpaEntity> findByAssignmentId(Long assignmentId);
    
    List<ExerciseJpaEntity> findByAssignmentIdOrderByOrderIndexAsc(Long assignmentId);
    
    @Modifying
    @Query("DELETE FROM ExerciseJpaEntity e WHERE e.assignmentId = :assignmentId")
    void deleteByAssignmentId(@Param("assignmentId") Long assignmentId);
}