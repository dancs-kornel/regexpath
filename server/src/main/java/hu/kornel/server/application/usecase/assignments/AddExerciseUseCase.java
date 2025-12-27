package hu.kornel.server.application.usecase.assignments;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.assignments.AddExerciseRequest;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.assignments.AssignmentAlreadySubmittedException;
import hu.kornel.server.domain.exception.assignments.AssignmentNotFoundException;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddExerciseUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    private final ExerciseRepositoryInterface exerciseRepository;
    
    @Transactional
    public Exercise execute(Long assignmentId, AddExerciseRequest request, Long userId) {
        log.debug("Adding exercise to assignment {} by user {}", assignmentId, userId);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        if (!assignment.isOwnedBy(userId)) {
            log.error("User {} attempted to add exercise to assignment {} owned by {}", 
                    userId, assignmentId, assignment.getTeacherId());
            throw new UnauthorizedAccessException("Csak a saját feladataidhoz adhatsz új gyakorlatot");
        }
        
        if (!assignment.canBeEdited()) {
            log.error("Cannot add exercise to assignment {} - status is {}", assignmentId, assignment.getStatus());
            throw new AssignmentAlreadySubmittedException();
        }
        
        Exercise exercise = Exercise.builder()
                .assignmentId(assignmentId)
                .type(request.getType())
                .orderIndex(request.getOrderIndex())
                .points(request.getPoints())
                .title(request.getTitle())
                .question(request.getQuestion())
                .configJson(request.getConfigJson())
                .explanation(request.getExplanation())
                .build();
        
        Exercise savedExercise = exerciseRepository.save(exercise);
        log.info("Exercise {} added to assignment {}", savedExercise.getId(), assignmentId);
        
        return savedExercise;
    }
}