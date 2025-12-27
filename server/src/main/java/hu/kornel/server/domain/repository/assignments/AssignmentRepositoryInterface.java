package hu.kornel.server.domain.repository.assignments;

import java.util.List;
import java.util.Optional;

import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentStatus;

public interface AssignmentRepositoryInterface {
    Optional<Assignment> findById(Long id);
    List<Assignment> findByTeacherId(Long teacherId);
    List<Assignment> findByTeacherIdAndStatus(Long teacherId, AssignmentStatus status);
    Assignment save(Assignment assignment);
    void deleteById(Long id);
    boolean existsById(Long id);
}