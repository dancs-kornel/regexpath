package hu.kornel.server.application.usecase.assignments;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentGroupAssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContinueAssignmentAttemptUseCase {
    
    private final AssignmentAttemptRepositoryInterface attemptRepository;
    private final AssignmentGroupAssignmentRepositoryInterface assignmentGroupAssignmentRepository;
    private final GroupMembershipRepositoryInterface groupMembershipRepository;
    
    @Transactional(readOnly = true)
    public AssignmentAttempt execute(Long assignmentId, Long groupId, Long userId) {
        log.debug("Continuing attempt for assignment {} in group {} by user {}", assignmentId, groupId, userId);
        
        boolean isAssigned = assignmentGroupAssignmentRepository
                .existsByAssignmentIdAndGroupId(assignmentId, groupId);
        if (!isAssigned) {
            throw new IllegalStateException("Ez a feladat nincs hozzárendelve ehhez a csoporthoz");
        }
        
        boolean isMember = groupMembershipRepository.existsByGroupIdAndStudentId(groupId, userId);
        if (!isMember) {
            throw new IllegalStateException("Nem vagy tagja ennek a csoportnak");
        }
        
        var attempts = attemptRepository.findByAssignmentIdAndStudentId(assignmentId, userId);
        AssignmentAttempt activeAttempt = attempts.stream()
                .filter(AssignmentAttempt::isInProgress)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nincs folyamatban lévő próbálkozásod"));
        
        log.info("Continuing attempt {} for assignment {} by user {}", 
                activeAttempt.getId(), assignmentId, userId);
        
        return activeAttempt;
    }
}
