package hu.kornel.server.application.usecase.assignments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentGroupAssignment;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentGroupAssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetGroupAssignmentsUseCase {
    
    private final AssignmentGroupAssignmentRepositoryInterface assignmentGroupAssignmentRepository;
    private final AssignmentRepositoryInterface assignmentRepository;
    private final GroupMembershipRepositoryInterface groupMembershipRepository;
    private final GroupRepositoryInterface groupRepository;
    
    @Transactional(readOnly = true)
    public List<Assignment> execute(Long groupId, Long userId) {
        log.debug("Fetching assignments for group {} by user {}", groupId, userId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    log.error("Group {} not found", groupId);
                    return new IllegalArgumentException("A csoport nem található");
                });
        
        boolean isOwner = group.isOwnedBy(userId);
        boolean isMember = groupMembershipRepository.existsByGroupIdAndStudentId(groupId, userId);
        
        if (!isMember && !isOwner) {
            log.error("User {} is not a member of group {}", userId, groupId);
            throw new IllegalStateException("Csak a csoporttagok láthatják a kiosztott feladatokat");
        }
        
        List<AssignmentGroupAssignment> groupAssignments = 
                assignmentGroupAssignmentRepository.findByGroupId(groupId);
        
        List<Assignment> assignments = groupAssignments.stream()
                .map(aga -> assignmentRepository.findById(aga.getAssignmentId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        
        log.info("Found {} assignments for group {}", assignments.size(), groupId);
        
        return assignments;
    }
}