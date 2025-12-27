package hu.kornel.server.application.usecase.assignments;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetTeacherAssignmentsUseCase {
    
    private final AssignmentRepositoryInterface assignmentRepository;
    private final UserRepositoryInterface userRepository;
    
    @Transactional(readOnly = true)
    public List<Assignment> execute(Long userId) {
        log.debug("Fetching assignments for teacher: {}", userId);
        
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(""));
        
        
        if (!user.isTeacher()) {
            log.error("User {} is not a teacher", userId);
            throw new UnauthorizedAccessException("Csak tanárok tekinthetik meg a feladataikat");
        }
        
        List<Assignment> assignments = assignmentRepository.findByTeacherId(userId);
        log.info("Found {} assignments for teacher {}", assignments.size(), userId);
        
        return assignments;
    }
}