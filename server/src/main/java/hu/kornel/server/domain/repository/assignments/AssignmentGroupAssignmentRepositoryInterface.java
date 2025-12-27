package hu.kornel.server.domain.repository.assignments;

import java.util.List;
import java.util.Optional;

import hu.kornel.server.domain.entities.assignments.AssignmentGroupAssignment;

public interface AssignmentGroupAssignmentRepositoryInterface {
    Optional<AssignmentGroupAssignment> findById(Long id);
    Optional<AssignmentGroupAssignment> findByAssignmentIdAndGroupId(Long assignmentId, Long groupId);
    List<AssignmentGroupAssignment> findByAssignmentId(Long assignmentId);
    List<AssignmentGroupAssignment> findByGroupId(Long groupId);
    AssignmentGroupAssignment save(AssignmentGroupAssignment assignmentGroupAssignment);
    void deleteById(Long id);
    void deleteByAssignmentIdAndGroupId(Long assignmentId, Long groupId);
    boolean existsByAssignmentIdAndGroupId(Long assignmentId, Long groupId);
}