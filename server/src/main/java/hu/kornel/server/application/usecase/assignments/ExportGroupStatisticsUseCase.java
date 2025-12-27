package hu.kornel.server.application.usecase.assignments;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;

import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.GroupMembership;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.entities.assignments.AssignmentGroupAssignment;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
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
public class ExportGroupStatisticsUseCase {
    
    private final GroupRepositoryInterface groupRepository;
    private final GroupMembershipRepositoryInterface membershipRepository;
    private final AssignmentGroupAssignmentRepositoryInterface assignmentGroupRepository;
    private final AssignmentRepositoryInterface assignmentRepository;
    private final AssignmentAttemptRepositoryInterface attemptRepository;
    private final UserRepositoryInterface userRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    @Transactional(readOnly = true)
    public byte[] execute(Long groupId, Long teacherId) {
        log.debug("Exporting statistics for group {} by teacher {}", groupId, teacherId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        
        if (!group.isOwnedBy(teacherId)) {
            throw new UnauthorizedAccessException("Csak a saját csoportjaid statisztikáit exportálhatod");
        }
        
        List<GroupMembership> memberships = membershipRepository.findByGroupId(groupId);
        List<Long> studentIds = memberships.stream()
                .map(GroupMembership::getStudentId)
                .collect(Collectors.toList());
        
        List<AssignmentGroupAssignment> assignments = assignmentGroupRepository.findByGroupId(groupId);
        List<Long> assignmentIds = assignments.stream()
                .map(AssignmentGroupAssignment::getAssignmentId)
                .collect(Collectors.toList());
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw)) {
            
            String[] header = {
                "Student Name", 
                "Email", 
                "Assignment", 
                "Started At", 
                "Submitted At", 
                "Attempts Used", 
                "Best Score",
                "Max Score",
                "Percentage",
                "Status"
            };
            writer.writeNext(header);
            
            for (Long studentId : studentIds) {
                User student = userRepository.findById(studentId).orElse(null);
                if (student == null) continue;
                
                for (Long assignmentId : assignmentIds) {
                    Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
                    if (assignment == null) continue;
                    
                    List<AssignmentAttempt> attempts = attemptRepository
                            .findByAssignmentIdAndStudentId(assignmentId, studentId);
                    
                    String[] row = buildCsvRow(student, assignment, attempts);
                    writer.writeNext(row);
                }
            }
            
            writer.flush();
            log.info("Exported CSV for group {} with {} students and {} assignments", 
                    groupId, studentIds.size(), assignmentIds.size());
            
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("Error generating CSV export", e);
            throw new RuntimeException("Hiba történt a CSV generálása során", e);
        }
    }
    
    private String[] buildCsvRow(User student, Assignment assignment, List<AssignmentAttempt> attempts) {
        String status;
        String startedAt;
        String submittedAt;
        String bestScore;
        String percentage;
        
        if (attempts.isEmpty()) {
            status = "NOT_STARTED";
            startedAt = "";
            submittedAt = "";
            bestScore = "";
            percentage = "";
        } else {
            boolean hasCompleted = attempts.stream().anyMatch(AssignmentAttempt::isCompleted);
            status = hasCompleted ? "COMPLETED" : "IN_PROGRESS";
            
            startedAt = attempts.stream()
                    .map(AssignmentAttempt::getStartedAt)
                    .min(java.time.LocalDateTime::compareTo)
                    .map(dt -> dt.format(DATE_FORMATTER))
                    .orElse("");
            
            submittedAt = attempts.stream()
                    .filter(AssignmentAttempt::isCompleted)
                    .map(AssignmentAttempt::getSubmittedAt)
                    .max(java.time.LocalDateTime::compareTo)
                    .map(dt -> dt.format(DATE_FORMATTER))
                    .orElse("");
            
            Integer best = attempts.stream()
                    .filter(AssignmentAttempt::isCompleted)
                    .map(AssignmentAttempt::getScore)
                    .max(Integer::compareTo)
                    .orElse(null);
            
            if (best != null) {
                bestScore = String.valueOf(best);
                double pct = (best * 100.0) / assignment.getTotalPoints();
                percentage = String.format("%.2f%%", pct);
            } else {
                bestScore = "";
                percentage = "";
            }
        }
        
        return new String[] {
            student.getUsername(),
            student.getEmail(),
            assignment.getTitle(),
            startedAt,
            submittedAt,
            String.valueOf(attempts.size()),
            bestScore,
            String.valueOf(assignment.getTotalPoints()),
            percentage,
            status
        };
    }
}