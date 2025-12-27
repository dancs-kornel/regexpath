package hu.kornel.server.application.usecase.assignments;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentGroupAssignment;
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
public class AssignToGroupUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    private final GroupRepositoryInterface groupRepository;
    private final AssignmentGroupAssignmentRepositoryInterface assignmentGroupAssignmentRepository;
    
    @Transactional
    public List<AssignmentGroupAssignment> execute(Long assignmentId, List<Long> groupIds, Long userId) {
        log.debug("Assigning assignment {} to {} groups by user {}", assignmentId, groupIds.size(), userId);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        if (!assignment.isOwnedBy(userId)) {
            log.error("User {} attempted to assign assignment {} owned by {}", 
                    userId, assignmentId, assignment.getTeacherId());
            throw new UnauthorizedAccessException("Csak a saját feladataidat oszthatod ki");
        }
        
        if (!assignment.isSubmitted()) {
            log.error("Cannot assign draft assignment {}", assignmentId);
            throw new IllegalStateException("Csak beküldött feladatokat lehet kiosztani csoportoknak");
        }
        
        List<AssignmentGroupAssignment> assignments = new ArrayList<>();
        
        for (Long groupId : groupIds) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException(groupId));
            
            if (!group.isOwnedBy(userId)) {
                log.error("User {} attempted to assign to group {} owned by {}", 
                        userId, groupId, group.getTeacherId());
                throw new UnauthorizedAccessException("Csak a saját csoportjaidhoz oszthatod ki a feladatokat");
            }
            
            if (assignmentGroupAssignmentRepository.existsByAssignmentIdAndGroupId(assignmentId, groupId)) {
                log.warn("Assignment {} already assigned to group {}, skipping", assignmentId, groupId);
                continue;
            }
            
            AssignmentGroupAssignment aga = AssignmentGroupAssignment.builder()
                    .assignmentId(assignmentId)
                    .groupId(groupId)
                    .assignedAt(LocalDateTime.now())
                    .build();
            
            AssignmentGroupAssignment saved = assignmentGroupAssignmentRepository.save(aga);
            assignments.add(saved);
            log.info("Assignment {} assigned to group {}", assignmentId, groupId);
        }
        
        return assignments;
    }
}