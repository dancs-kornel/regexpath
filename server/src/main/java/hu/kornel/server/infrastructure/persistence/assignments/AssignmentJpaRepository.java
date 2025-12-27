package hu.kornel.server.infrastructure.persistence.assignments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.kornel.server.domain.entities.assignments.AssignmentStatus;

@Repository
public interface AssignmentJpaRepository extends JpaRepository<AssignmentJpaEntity, Long> {
    
    List<AssignmentJpaEntity> findByTeacherId(Long teacherId);
    
    List<AssignmentJpaEntity> findByTeacherIdAndStatus(Long teacherId, AssignmentStatus status);
    
    @Modifying
    @Query("DELETE FROM AssignmentJpaEntity a WHERE a.id = :id")
    void deleteAssignmentById(@Param("id") Long id);
}