package hu.kornel.server.application.usecase.assignments;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.assignments.AssignmentNotFoundException;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetAssignmentDetailsUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    private final ExerciseRepositoryInterface exerciseRepository;
    
    @Transactional(readOnly = true)
    public Assignment execute(Long assignmentId, Long userId) {
        log.debug("Fetching assignment details for assignment {} by user {}", assignmentId, userId);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        if (!assignment.isOwnedBy(userId)) {
            log.error("User {} attempted to view assignment {} owned by {}", 
                    userId, assignmentId, assignment.getTeacherId());
            throw new UnauthorizedAccessException("Csak a saját feladataidat tekintheted meg");
        }
        
        List<Exercise> exercises = exerciseRepository.findByAssignmentIdOrderByOrderIndex(assignmentId);
        assignment.setExercises(exercises);
        
        log.info("Assignment {} with {} exercises fetched", assignmentId, exercises.size());
        
        return assignment;
    }
}