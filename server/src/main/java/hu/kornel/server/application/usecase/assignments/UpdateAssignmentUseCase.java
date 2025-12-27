package hu.kornel.server.application.usecase.assignments;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.assignments.UpdateAssignmentRequest;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.assignments.AssignmentAlreadySubmittedException;
import hu.kornel.server.domain.exception.assignments.AssignmentNotFoundException;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateAssignmentUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    
    @Transactional
    public Assignment execute(Long assignmentId, UpdateAssignmentRequest request, Long userId) {
        log.debug("Updating assignment {} by user {}", assignmentId, userId);
        
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        
        if (!assignment.isOwnedBy(userId)) {
            log.error("User {} attempted to update assignment {} owned by {}", 
                    userId, assignmentId, assignment.getTeacherId());
            throw new UnauthorizedAccessException("Csak a saját feladataidat szerkesztheted");
        }
        
        
        if (!assignment.canBeEdited()) {
            log.error("Cannot edit assignment {} - status is {}", assignmentId, assignment.getStatus());
            throw new AssignmentAlreadySubmittedException();
        }
        
        
        if (request.getTitle() != null) {
            assignment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }
        if (request.getDueDate() != null) {
            assignment.setDueDate(request.getDueDate());
        }
        if (request.getTimeLimitMinutes() != null) {
            assignment.setTimeLimitMinutes(request.getTimeLimitMinutes());
        }
        if (request.getMaxAttempts() != null) {
            assignment.setMaxAttempts(request.getMaxAttempts());
        }
        
        assignment.setUpdatedAt(LocalDateTime.now());
        
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment {} updated successfully", assignmentId);
        
        return updatedAssignment;
    }
}