package hu.kornel.server.application.usecase.assignments;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.assignments.AssignmentNotFoundException;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentGroupAssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UnassignFromGroupUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    private final GroupRepositoryInterface groupRepository;
    private final AssignmentGroupAssignmentRepositoryInterface assignmentGroupAssignmentRepository;
    
    @Transactional
    public void execute(Long assignmentId, Long groupId, Long userId) {
        log.debug("Unassigning assignment {} from group {} by user {}", assignmentId, groupId, userId);
        
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        
        if (!assignment.isOwnedBy(userId)) {
            log.error("User {} attempted to unassign assignment {} owned by {}", 
                    userId, assignmentId, assignment.getTeacherId());
            throw new UnauthorizedAccessException("Csak a saját feladataidat vonhatod vissza");
        }
        
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        
        if (!group.isOwnedBy(userId)) {
            log.error("User {} attempted to unassign from group {} owned by {}", 
                    userId, groupId, group.getTeacherId());
            throw new UnauthorizedAccessException("Csak a saját csoportjaidból vonhatod vissza a feladatokat");
        }
        
        
        assignmentGroupAssignmentRepository.deleteByAssignmentIdAndGroupId(assignmentId, groupId);
        log.info("Assignment {} unassigned from group {}", assignmentId, groupId);
    }
}