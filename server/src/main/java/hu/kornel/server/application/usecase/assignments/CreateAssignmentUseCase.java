package hu.kornel.server.application.usecase.assignments;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.assignments.AddExerciseRequest;
import hu.kornel.server.application.dto.assignments.CreateAssignmentRequest;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentStatus;
import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateAssignmentUseCase {

    private final AssignmentRepositoryInterface assignmentRepository;
    private final UserRepositoryInterface userRepository;
    private final ExerciseRepositoryInterface exerciseRepository;

    @Transactional
    public Assignment execute(CreateAssignmentRequest request, Long userId) {
        log.debug("Creating assignment for teacher: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(""));

        if (!user.isTeacher()) {
            log.error("User {} is not a teacher", userId);
            throw new UnauthorizedAccessException("Csak tanárok hozhatnak létre feladatokat");
        }

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .teacherId(userId)
                .status(AssignmentStatus.DRAFT)
                .dueDate(request.getDueDate())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .maxAttempts(request.getMaxAttempts())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);

        if (request.getExercises() != null && !request.getExercises().isEmpty()) {
            for (AddExerciseRequest exerciseReq : request.getExercises()) {
                Exercise exercise = Exercise.builder()
                        .assignmentId(savedAssignment.getId())
                        .type(exerciseReq.getType())
                        .orderIndex(exerciseReq.getOrderIndex())
                        .points(exerciseReq.getPoints())
                        .title(exerciseReq.getTitle() != null ? exerciseReq.getTitle() : exerciseReq.getQuestion())
                        .question(exerciseReq.getQuestion())
                        .configJson(exerciseReq.getConfigJson())
                        .explanation(exerciseReq.getExplanation())
                        .build();

                exerciseRepository.save(exercise);
            }
        }

        log.info("Assignment created successfully with id: {}", savedAssignment.getId());

        return savedAssignment;
    }
}