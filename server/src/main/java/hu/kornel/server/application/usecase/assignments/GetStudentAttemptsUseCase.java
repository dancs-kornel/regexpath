package hu.kornel.server.application.usecase.assignments;

import java.util.List;

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
public class GetStudentAttemptsUseCase {
    
    private final AssignmentAttemptRepositoryInterface attemptRepository;
    private final AssignmentGroupAssignmentRepositoryInterface assignmentGroupAssignmentRepository;
    private final GroupMembershipRepositoryInterface groupMembershipRepository;
    
    @Transactional(readOnly = true)
    public List<AssignmentAttempt> execute(Long assignmentId, Long groupId, Long userId) {
        log.debug("Fetching attempts for assignment {} in group {} by user {}", 
                assignmentId, groupId, userId);
        
        boolean isAssigned = assignmentGroupAssignmentRepository
                .existsByAssignmentIdAndGroupId(assignmentId, groupId);
        if (!isAssigned) {
            throw new IllegalStateException("Ez a feladat nincs hozzárendelve ehhez a csoporthoz");
        }
        
        boolean isMember = groupMembershipRepository.existsByGroupIdAndStudentId(groupId, userId);
        if (!isMember) {
            throw new IllegalStateException("Nem vagy tagja ennek a csoportnak");
        }
        
        List<AssignmentAttempt> attempts = attemptRepository
                .findByAssignmentIdAndStudentId(assignmentId, userId);
        
        log.info("Found {} attempts for user {} on assignment {}", attempts.size(), userId, assignmentId);
        
        return attempts;
    }
}