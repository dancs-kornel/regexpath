package hu.kornel.server.infrastructure.persistence.assignments;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentAttemptJpaRepository extends JpaRepository<AssignmentAttemptJpaEntity, Long> {
    List<AssignmentAttemptJpaEntity> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    List<AssignmentAttemptJpaEntity> findByStudentId(Long studentId);
    List<AssignmentAttemptJpaEntity> findByAssignmentIdAndGroupId(Long assignmentId, Long groupId);
    Optional<AssignmentAttemptJpaEntity> findByAssignmentIdAndStudentIdAndAttemptNumber(Long assignmentId, Long studentId, Integer attemptNumber);
    int countByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
}