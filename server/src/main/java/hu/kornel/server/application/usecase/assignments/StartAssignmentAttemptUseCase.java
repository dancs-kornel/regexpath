package hu.kornel.server.application.usecase.assignments;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.exception.assignments.AssignmentNotFoundException;
import hu.kornel.server.domain.exception.assignments.MaxAttemptsExceededException;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentGroupAssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StartAssignmentAttemptUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    private final AssignmentAttemptRepositoryInterface attemptRepository;
    private final AssignmentGroupAssignmentRepositoryInterface assignmentGroupAssignmentRepository;
    private final GroupMembershipRepositoryInterface groupMembershipRepository;
    
    @Transactional
    public AssignmentAttempt execute(Long assignmentId, Long groupId, Long userId) {
        log.debug("Starting attempt for assignment {} in group {} by user {}", assignmentId, groupId, userId);
        
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        
        boolean isAssigned = assignmentGroupAssignmentRepository
                .existsByAssignmentIdAndGroupId(assignmentId, groupId);
        if (!isAssigned) {
            throw new IllegalStateException("Ez a feladat nincs hozzárendelve ehhez a csoporthoz");
        }
        
        
        boolean isMember = groupMembershipRepository.existsByGroupIdAndStudentId(groupId, userId);
        if (!isMember) {
            throw new IllegalStateException("Nem vagy tagja ennek a csoportnak");
        }
        
        
        if (assignment.isExpired()) {
            throw new IllegalStateException("A feladat határideje lejárt");
        }
        
        
        int attemptCount = attemptRepository.countByAssignmentIdAndStudentId(assignmentId, userId);
        if (!assignment.hasUnlimitedAttempts() && attemptCount >= assignment.getMaxAttempts()) {
            throw new MaxAttemptsExceededException(assignment.getMaxAttempts());
        }
        
        
        var existingAttempts = attemptRepository.findByAssignmentIdAndStudentId(assignmentId, userId);
        for (AssignmentAttempt attempt : existingAttempts) {
            if (attempt.isInProgress()) {
                log.warn("User {} already has an active attempt {} for assignment {}", 
                        userId, attempt.getId(), assignmentId);
                throw new IllegalStateException("Már van folyamatban lévő próbálkozásod. Fejezd be vagy várd meg a lejáratot!");
            }
        }
        
        
        LocalDateTime expiresAt = null;
        if (assignment.hasTimeLimit()) {
            expiresAt = LocalDateTime.now().plusMinutes(assignment.getTimeLimitMinutes());
            
            if (assignment.getDueDate() != null && expiresAt.isAfter(assignment.getDueDate())) {
                expiresAt = assignment.getDueDate();
            }
        } else if (assignment.getDueDate() != null) {
            expiresAt = assignment.getDueDate();
        }
        
        
        AssignmentAttempt attempt = AssignmentAttempt.builder()
                .assignmentId(assignmentId)
                .studentId(userId)
                .groupId(groupId)
                .attemptNumber(attemptCount + 1)
                .maxScore(assignment.getTotalPoints())
                .startedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .completed(false)
                .build();
        
        AssignmentAttempt savedAttempt = attemptRepository.save(attempt);
        log.info("Attempt {} started for assignment {} by user {}", savedAttempt.getId(), assignmentId, userId);
        
        return savedAttempt;
    }
}