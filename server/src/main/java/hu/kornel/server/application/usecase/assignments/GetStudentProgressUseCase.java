package hu.kornel.server.application.usecase.assignments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.assignments.StudentProgressResponse;
import hu.kornel.server.application.dto.assignments.StudentProgressResponse.AssignmentProgressRow;
import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.entities.assignments.AssignmentGroupAssignment;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.UserNotFoundException;
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
public class GetStudentProgressUseCase {
    
    private final GroupRepositoryInterface groupRepository;
    private final UserRepositoryInterface userRepository;
    private final GroupMembershipRepositoryInterface membershipRepository;
    private final AssignmentGroupAssignmentRepositoryInterface assignmentGroupRepository;
    private final AssignmentRepositoryInterface assignmentRepository;
    private final AssignmentAttemptRepositoryInterface attemptRepository;
    
    @Transactional(readOnly = true)
    public StudentProgressResponse execute(Long groupId, Long studentId, Long teacherId) {
        log.debug("Fetching progress for student {} in group {} by teacher {}", 
                studentId, groupId, teacherId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        
        if (!group.isOwnedBy(teacherId)) {
            throw new UnauthorizedAccessException("Csak a saját csoportjaid diákjainak statisztikáit tekintheted meg");
        }
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(""));
        
        boolean isMember = membershipRepository.existsByGroupIdAndStudentId(groupId, studentId);
        if (!isMember) {
            throw new IllegalStateException("Ez a diák nem tagja ennek a csoportnak");
        }
        
        List<AssignmentGroupAssignment> assignments = assignmentGroupRepository.findByGroupId(groupId);
        List<Long> assignmentIds = assignments.stream()
                .map(AssignmentGroupAssignment::getAssignmentId)
                .collect(Collectors.toList());
        
        List<AssignmentProgressRow> progress = assignmentIds.stream()
                .map(assignmentId -> buildProgressRow(assignmentId, studentId))
                .filter(row -> row != null)
                .collect(Collectors.toList());
        
        int completed = (int) progress.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .count();
        
        int inProgress = (int) progress.stream()
                .filter(p -> "IN_PROGRESS".equals(p.getStatus()))
                .count();
        
        int notStarted = (int) progress.stream()
                .filter(p -> "NOT_STARTED".equals(p.getStatus()))
                .count();
        
        int totalPointsPossible = progress.stream()
                .mapToInt(AssignmentProgressRow::getTotalPoints)
                .sum();
        
        
        int totalPointsEarned = progress.stream()
                .mapToInt(p -> p.getBestScore() != null ? p.getBestScore() : 0)
                .sum();
        
        Double completionRate = assignmentIds.isEmpty() ? 0.0 : 
                (completed * 100.0) / assignmentIds.size();
        
        log.info("Student {} progress in group {}: {} assignments, {} completed, {}/{} points", 
                studentId, groupId, assignmentIds.size(), completed, totalPointsEarned, totalPointsPossible);
        
        return StudentProgressResponse.builder()
                .studentId(student.getId())
                .studentName(student.getUsername())
                .studentEmail(student.getEmail())
                .groupId(group.getId())
                .groupName(group.getName())
                .totalAssignments(assignmentIds.size())
                .assignmentsCompleted(completed)
                .assignmentsInProgress(inProgress)
                .assignmentsNotStarted(notStarted)
                .totalPointsEarned(totalPointsEarned)
                .totalPointsPossible(totalPointsPossible)
                .completionRate(completionRate)
                .assignmentProgress(progress)
                .build();
    }
    
    private AssignmentProgressRow buildProgressRow(Long assignmentId, Long studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) {
            return null;
        }
        
        List<AssignmentAttempt> attempts = attemptRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId);
        
        
        String status;
        if (attempts.isEmpty()) {
            status = "NOT_STARTED";
        } else {
            boolean hasCompleted = attempts.stream().anyMatch(AssignmentAttempt::isCompleted);
            status = hasCompleted ? "COMPLETED" : "IN_PROGRESS";
        }
        
        
        Integer bestScore = attempts.stream()
                .filter(AssignmentAttempt::isCompleted)
                .map(AssignmentAttempt::getScore)
                .max(Integer::compareTo)
                .orElse(null);
        
        Double percentageScore = null;
        if (bestScore != null) {
            percentageScore = (bestScore * 100.0) / assignment.getTotalPoints();
        }
        
        
        Long bestAttemptId = attempts.stream()
                .filter(AssignmentAttempt::isCompleted)
                .filter(a -> a.getScore().equals(bestScore))
                .map(AssignmentAttempt::getId)
                .findFirst()
                .orElse(null);
        
        
        LocalDateTime firstStarted = attempts.stream()
                .map(AssignmentAttempt::getStartedAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        
        LocalDateTime lastSubmitted = attempts.stream()
                .filter(AssignmentAttempt::isCompleted)
                .map(AssignmentAttempt::getSubmittedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        return AssignmentProgressRow.builder()
                .assignmentId(assignment.getId())
                .assignmentTitle(assignment.getTitle())
                .dueDate(assignment.getDueDate())
                .totalPoints(assignment.getTotalPoints())
                .status(status)
                .attemptsUsed(attempts.size())
                .maxAttempts(assignment.getMaxAttempts())
                .bestScore(bestScore)
                .percentageScore(percentageScore)
                .firstStartedAt(firstStarted)
                .lastSubmittedAt(lastSubmitted)
                .bestAttemptId(bestAttemptId)
                .build();
    }
}