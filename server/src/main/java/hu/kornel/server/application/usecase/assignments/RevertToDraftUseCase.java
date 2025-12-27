package hu.kornel.server.application.usecase.assignments;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.assignments.AssignmentNotFoundException;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RevertToDraftUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    
    @Transactional
    public Assignment execute(Long assignmentId, Long userId) {
        log.debug("Reverting assignment {} to draft by user {}", assignmentId, userId);
        
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        
        if (!assignment.isOwnedBy(userId)) {
            log.error("User {} attempted to revert assignment {} owned by {}", 
                    userId, assignmentId, assignment.getTeacherId());
            throw new UnauthorizedAccessException("Csak a saját feladataidat állíthatod vissza vázlat állapotba");
        }
        
        
        assignment.revertToDraft();
        
        Assignment revertedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment {} reverted to draft", assignmentId);
        
        return revertedAssignment;
    }
}