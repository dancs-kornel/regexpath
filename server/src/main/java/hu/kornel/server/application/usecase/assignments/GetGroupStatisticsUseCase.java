package hu.kornel.server.application.usecase.assignments;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.assignments.GroupStatisticsResponse;
import hu.kornel.server.application.dto.assignments.GroupStatisticsResponse.AssignmentSummaryRow;
import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.GroupMembership;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.entities.assignments.AssignmentGroupAssignment;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentGroupAssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetGroupStatisticsUseCase {
    
    private final GroupRepositoryInterface groupRepository;
    private final GroupMembershipRepositoryInterface membershipRepository;
    private final AssignmentGroupAssignmentRepositoryInterface assignmentGroupRepository;
    private final AssignmentRepositoryInterface assignmentRepository;
    private final AssignmentAttemptRepositoryInterface attemptRepository;
    
    @Transactional(readOnly = true)
    public GroupStatisticsResponse execute(Long groupId, Long teacherId) {
        log.debug("Fetching group statistics for group {} by teacher {}", groupId, teacherId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        
        if (!group.isOwnedBy(teacherId)) {
            throw new UnauthorizedAccessException("Csak a saját csoportjaid statisztikáit tekintheted meg");
        }
        
        List<GroupMembership> memberships = membershipRepository.findByGroupId(groupId);
        int totalStudents = memberships.size();
        List<Long> studentIds = memberships.stream()
                .map(GroupMembership::getStudentId)
                .collect(Collectors.toList());
        
        List<AssignmentGroupAssignment> assignments = assignmentGroupRepository.findByGroupId(groupId);
        List<Long> assignmentIds = assignments.stream()
                .map(AssignmentGroupAssignment::getAssignmentId)
                .collect(Collectors.toList());
        
        List<AssignmentSummaryRow> summaries = assignmentIds.stream()
                .map(assignmentId -> buildAssignmentSummary(assignmentId, groupId, studentIds))
                .filter(summary -> summary != null)
                .collect(Collectors.toList());
        
        Double overallCompletionRate = summaries.isEmpty() ? 0.0 :
                summaries.stream()
                        .mapToDouble(AssignmentSummaryRow::getCompletionRate)
                        .average()
                        .orElse(0.0);
        
        log.info("Group {} statistics: {} students, {} assignments, completion rate: {}%", 
                groupId, totalStudents, assignmentIds.size(), overallCompletionRate);
        
        return GroupStatisticsResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .totalStudents(totalStudents)
                .totalAssignments(assignmentIds.size())
                .overallCompletionRate(overallCompletionRate)
                .assignmentSummaries(summaries)
                .build();
    }
    
    private AssignmentSummaryRow buildAssignmentSummary(
            Long assignmentId, 
            Long groupId,
            List<Long> studentIds) {
        
        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) {
            return null;
        }
        
        List<AssignmentAttempt> attempts = attemptRepository
                .findByAssignmentIdAndGroupId(assignmentId, groupId);
        
        int completed = 0;
        int inProgress = 0;
        int notStarted = 0;
        
        for (Long studentId : studentIds) {
            List<AssignmentAttempt> studentAttempts = attempts.stream()
                    .filter(a -> a.getStudentId().equals(studentId))
                    .collect(Collectors.toList());
            
            if (studentAttempts.isEmpty()) {
                notStarted++;
            } else {
                boolean hasCompleted = studentAttempts.stream()
                        .anyMatch(AssignmentAttempt::isCompleted);
                if (hasCompleted) {
                    completed++;
                } else {
                    inProgress++;
                }
            }
        }
        
        Double averageScore = studentIds.stream()
                .map(studentId -> attempts.stream()
                        .filter(a -> a.getStudentId().equals(studentId))
                        .filter(AssignmentAttempt::isCompleted)
                        .map(AssignmentAttempt::getScore)
                        .max(Integer::compareTo)
                        .orElse(null))
                .filter(score -> score != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        
        Double completionRate = studentIds.isEmpty() ? 0.0 : 
                (completed * 100.0) / studentIds.size();
        
        return AssignmentSummaryRow.builder()
                .assignmentId(assignment.getId())
                .assignmentTitle(assignment.getTitle())
                .dueDate(assignment.getDueDate())
                .totalPoints(assignment.getTotalPoints())
                .studentsCompleted(completed)
                .studentsInProgress(inProgress)
                .studentsNotStarted(notStarted)
                .averageScore(averageScore)
                .completionRate(completionRate)
                .build();
    }
}