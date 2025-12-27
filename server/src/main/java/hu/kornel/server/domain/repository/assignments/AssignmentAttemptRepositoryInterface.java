package hu.kornel.server.domain.repository.assignments;

import java.util.List;
import java.util.Optional;

import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;

public interface AssignmentAttemptRepositoryInterface {
    Optional<AssignmentAttempt> findById(Long id);
    List<AssignmentAttempt> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    List<AssignmentAttempt> findByStudentId(Long studentId);
    List<AssignmentAttempt> findByAssignmentIdAndGroupId(Long assignmentId, Long groupId);
    Optional<AssignmentAttempt> findByAssignmentIdAndStudentIdAndAttemptNumber(Long assignmentId, Long studentId, Integer attemptNumber);
    int countByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    AssignmentAttempt save(AssignmentAttempt attempt);
    void deleteById(Long id);
}