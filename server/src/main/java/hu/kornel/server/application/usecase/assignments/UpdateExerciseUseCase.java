package hu.kornel.server.application.usecase.assignments;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.assignments.UpdateExerciseRequest;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.assignments.AssignmentAlreadySubmittedException;
import hu.kornel.server.domain.exception.assignments.AssignmentNotFoundException;
import hu.kornel.server.domain.exception.assignments.ExerciseNotFoundException;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateExerciseUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    private final ExerciseRepositoryInterface exerciseRepository;
    
    @Transactional
    public Exercise execute(Long assignmentId, Long exerciseId, UpdateExerciseRequest request, Long userId) {
        log.debug("Updating exercise {} in assignment {} by user {}", exerciseId, assignmentId, userId);
        
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        
        if (!assignment.isOwnedBy(userId)) {
            log.error("User {} attempted to update exercise in assignment {} owned by {}",
                     userId, assignmentId, assignment.getTeacherId());
            throw new UnauthorizedAccessException("Csak a saját feladataidban módosíthatsz gyakorlatot");
        }
        
        
        if (!assignment.canBeEdited()) {
            log.error("Cannot update exercise in assignment {} - status is {}", assignmentId, assignment.getStatus());
            throw new AssignmentAlreadySubmittedException();
        }
        
        
        Exercise existingExercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));
        
        
        if (!existingExercise.getAssignmentId().equals(assignmentId)) {
            log.error("Exercise {} does not belong to assignment {}", exerciseId, assignmentId);
            throw new UnauthorizedAccessException("Ez a gyakorlat nem ehhez a feladathoz tartozik");
        }
        
        
        Exercise updatedExercise = Exercise.builder()
                .id(exerciseId)
                .assignmentId(assignmentId)
                .type(request.getType())
                .orderIndex(request.getOrderIndex())
                .points(request.getPoints())
                .title(request.getTitle())
                .question(request.getQuestion())
                .configJson(request.getConfigJson())
                .explanation(request.getExplanation())
                .build();
        
        Exercise savedExercise = exerciseRepository.save(updatedExercise);
        log.info("Exercise {} updated in assignment {}", exerciseId, assignmentId);
        
        return savedExercise;
    }
}