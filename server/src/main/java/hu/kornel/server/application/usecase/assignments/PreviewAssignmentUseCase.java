package hu.kornel.server.application.usecase.assignments;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreviewAssignmentUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    
    @Transactional(readOnly = true)
    public Assignment execute(Long assignmentId, Long teacherId) {
        log.debug("Teacher {} requesting preview of assignment {}", teacherId, assignmentId);
        
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    log.error("Assignment {} not found", assignmentId);
                    return new IllegalArgumentException("A feladat nem található");
                });
        
        
        if (!assignment.isOwnedBy(teacherId)) {
            log.error("Teacher {} attempted to preview assignment {} which they don't own", 
                    teacherId, assignmentId);
            throw new IllegalStateException("Csak a saját feladatait tekintheti meg");
        }
        
        log.info("Teacher {} previewing assignment {} (status: {}, {} exercises)", 
                teacherId, assignmentId, assignment.getStatus(), assignment.getExerciseCount());
        
        return assignment;
    }
}