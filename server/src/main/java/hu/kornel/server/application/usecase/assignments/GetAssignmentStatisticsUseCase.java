package hu.kornel.server.application.usecase.assignments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.assignments.AssignmentStatisticsResponse;
import hu.kornel.server.application.dto.assignments.AssignmentStatisticsResponse.StudentStatisticsRow;
import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.GroupMembership;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.assignments.AssignmentNotFoundException;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentGroupAssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetAssignmentStatisticsUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    private final GroupRepositoryInterface groupRepository;
    private final GroupMembershipRepositoryInterface membershipRepository;
    private final AssignmentAttemptRepositoryInterface attemptRepository;
    private final AssignmentGroupAssignmentRepositoryInterface assignmentGroupRepository;
    private final UserRepositoryInterface userRepository;
    
    @Transactional(readOnly = true)
    public AssignmentStatisticsResponse execute(Long assignmentId, Long groupId, Long teacherId) {
        log.debug("Fetching statistics for assignment {} in group {} by teacher {}", 
                assignmentId, groupId, teacherId);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        if (!assignment.isOwnedBy(teacherId)) {
            throw new UnauthorizedAccessException("Csak a saját feladataid statisztikáit tekintheted meg");
        }
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        
        if (!group.isOwnedBy(teacherId)) {
            throw new UnauthorizedAccessException("Csak a saját csoportjaid statisztikáit tekintheted meg");
        }
        
        boolean isAssigned = assignmentGroupRepository
                .existsByAssignmentIdAndGroupId(assignmentId, groupId);
        if (!isAssigned) {
            throw new IllegalStateException("Ez a feladat nincs hozzárendelve ehhez a csoporthoz");
        }
        
        List<GroupMembership> memberships = membershipRepository.findByGroupId(groupId);
        List<Long> studentIds = memberships.stream()
                .map(GroupMembership::getStudentId)
                .collect(Collectors.toList());
        
        List<AssignmentAttempt> allAttempts = attemptRepository
                .findByAssignmentIdAndGroupId(assignmentId, groupId);
        
        List<StudentStatisticsRow> studentStats = studentIds.stream()
                .map(studentId -> buildStudentRow(studentId, allAttempts, assignment))
                .collect(Collectors.toList());
        
        int notStarted = (int) studentStats.stream()
                .filter(s -> "NOT_STARTED".equals(s.getStatus()))
                .count();
        
        int inProgress = (int) studentStats.stream()
                .filter(s -> "IN_PROGRESS".equals(s.getStatus()))
                .count();
        
        int completed = (int) studentStats.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count();
        
        Double averageScore = studentStats.stream()
                .filter(s -> s.getBestScore() != null)
                .mapToInt(StudentStatisticsRow::getBestScore)
                .average()
                .orElse(0.0);
        
        Double completionRate = studentIds.isEmpty() ? 0.0 : 
                (completed * 100.0) / studentIds.size();
        
        log.info("Assignment {} statistics: {} students, {} completed, avg score: {}", 
                assignmentId, studentIds.size(), completed, averageScore);
        
        return AssignmentStatisticsResponse.builder()
                .assignmentId(assignment.getId())
                .assignmentTitle(assignment.getTitle())
                .totalPoints(assignment.getTotalPoints())
                .maxAttempts(assignment.getMaxAttempts())
                .dueDate(assignment.getDueDate())
                .totalStudents(studentIds.size())
                .studentsNotStarted(notStarted)
                .studentsInProgress(inProgress)
                .studentsCompleted(completed)
                .averageScore(averageScore)
                .completionRate(completionRate)
                .studentStats(studentStats)
                .build();
    }
    
    private StudentStatisticsRow buildStudentRow(
            Long studentId, 
            List<AssignmentAttempt> allAttempts,
            Assignment assignment) {
        
        User student = userRepository.findById(studentId).orElse(null);
        if (student == null) {
            return null;
        }
        
        List<AssignmentAttempt> studentAttempts = allAttempts.stream()
                .filter(a -> a.getStudentId().equals(studentId))
                .collect(Collectors.toList());
        
        String status;
        if (studentAttempts.isEmpty()) {
            status = "NOT_STARTED";
        } else {
            boolean hasCompleted = studentAttempts.stream().anyMatch(AssignmentAttempt::isCompleted);
            status = hasCompleted ? "COMPLETED" : "IN_PROGRESS";
        }
        
        Integer bestScore = studentAttempts.stream()
                .filter(AssignmentAttempt::isCompleted)
                .map(AssignmentAttempt::getScore)
                .max(Integer::compareTo)
                .orElse(null);
        
        Double percentageScore = null;
        if (bestScore != null) {
            percentageScore = (bestScore * 100.0) / assignment.getTotalPoints();
        }
        
        Long bestAttemptId = studentAttempts.stream()
                .filter(AssignmentAttempt::isCompleted)
                .filter(a -> a.getScore().equals(bestScore))
                .map(AssignmentAttempt::getId)
                .findFirst()
                .orElse(null);
        
        LocalDateTime firstStarted = studentAttempts.stream()
                .map(AssignmentAttempt::getStartedAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        
        LocalDateTime lastSubmitted = studentAttempts.stream()
                .filter(AssignmentAttempt::isCompleted)
                .map(AssignmentAttempt::getSubmittedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        return StudentStatisticsRow.builder()
                .studentId(studentId)
                .studentName(student.getUsername())
                .studentEmail(student.getEmail())
                .status(status)
                .attemptsUsed(studentAttempts.size())
                .bestScore(bestScore)
                .percentageScore(percentageScore)
                .firstStartedAt(firstStarted)
                .lastSubmittedAt(lastSubmitted)
                .bestAttemptId(bestAttemptId)
                .build();
    }
}