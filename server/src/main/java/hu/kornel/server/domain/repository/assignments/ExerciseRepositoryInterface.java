package hu.kornel.server.domain.repository.assignments;

import java.util.List;
import java.util.Optional;

import hu.kornel.server.domain.entities.assignments.Exercise;

public interface ExerciseRepositoryInterface {
    Optional<Exercise> findById(Long id);
    List<Exercise> findByAssignmentId(Long assignmentId);
    List<Exercise> findByAssignmentIdOrderByOrderIndex(Long assignmentId);
    Exercise save(Exercise exercise);
    void deleteById(Long id);
    void deleteByAssignmentId(Long assignmentId);
}