package hu.kornel.server.application.usecase.assignments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.kornel.server.application.dto.assignments.AddExerciseRequest;
import hu.kornel.server.application.dto.assignments.CreateAssignmentRequest;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.UserRole;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentStatus;
import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.entities.assignments.ExerciseType;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseRepositoryInterface;

@ExtendWith(MockitoExtension.class)
class CreateAssignmentUseCaseTest {

    @Mock
    AssignmentRepositoryInterface assignmentRepository;

    @Mock
    UserRepositoryInterface userRepository;

    @Mock
    ExerciseRepositoryInterface exerciseRepository;

    @InjectMocks
    CreateAssignmentUseCase useCase;

    @Test
    @DisplayName("execute: teacher creates assignment with exercises successfully")
    void execute_success_withExercises() {
        Long teacherId = 1L;
        User teacher = User.builder()
                .id(teacherId)
                .username("teacher1")
                .email("teacher@example.com")
                .role(UserRole.TEACHER)
                .active(true)
                .build();

        CreateAssignmentRequest request = mock(CreateAssignmentRequest.class);
        when(request.getTitle()).thenReturn("Java Basics Assignment");
        when(request.getDescription()).thenReturn("Learn Java fundamentals");
        when(request.getDueDate()).thenReturn(LocalDateTime.now().plusDays(7));
        when(request.getTimeLimitMinutes()).thenReturn(60);
        when(request.getMaxAttempts()).thenReturn(3);

        AddExerciseRequest exercise1 = mock(AddExerciseRequest.class);
        when(exercise1.getType()).thenReturn(ExerciseType.REGEX_SANDBOX);
        when(exercise1.getOrderIndex()).thenReturn(1);
        when(exercise1.getPoints()).thenReturn(10);
        when(exercise1.getQuestion()).thenReturn("Write a regex");
        when(exercise1.getTitle()).thenReturn("Regex Exercise");
        when(exercise1.getConfigJson()).thenReturn("{}");

        when(request.getExercises()).thenReturn(List.of(exercise1));

        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment assignment = inv.getArgument(0);
            assignment.setId(100L);
            return assignment;
        });
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(inv -> inv.getArgument(0));

        Assignment result = useCase.execute(request, teacherId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTitle()).isEqualTo("Java Basics Assignment");
        assertThat(result.getDescription()).isEqualTo("Learn Java fundamentals");
        assertThat(result.getTeacherId()).isEqualTo(teacherId);
        assertThat(result.getStatus()).isEqualTo(AssignmentStatus.DRAFT);
        assertThat(result.getTimeLimitMinutes()).isEqualTo(60);
        assertThat(result.getMaxAttempts()).isEqualTo(3);

        verify(userRepository).findById(teacherId);
        verify(assignmentRepository).save(any(Assignment.class));
        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    @DisplayName("execute: teacher creates assignment without exercises")
    void execute_success_noExercises() {
        Long teacherId = 2L;
        User teacher = User.builder()
                .id(teacherId)
                .role(UserRole.TEACHER)
                .build();

        CreateAssignmentRequest request = mock(CreateAssignmentRequest.class);
        when(request.getTitle()).thenReturn("Empty Assignment");
        when(request.getDescription()).thenReturn("To be filled");
        when(request.getExercises()).thenReturn(new ArrayList<>());

        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment assignment = inv.getArgument(0);
            assignment.setId(101L);
            return assignment;
        });

        Assignment result = useCase.execute(request, teacherId);

        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getStatus()).isEqualTo(AssignmentStatus.DRAFT);

        verify(assignmentRepository).save(any(Assignment.class));
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: user not found throws UserNotFoundException")
    void execute_userNotFound_throws() {
        Long userId = 999L;
        CreateAssignmentRequest request = mock(CreateAssignmentRequest.class);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request, userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: student user throws UnauthorizedAccessException")
    void execute_studentUser_throws() {
        Long studentId = 3L;
        User student = User.builder()
                .id(studentId)
                .username("student1")
                .role(UserRole.STUDENT)
                .build();

        CreateAssignmentRequest request = mock(CreateAssignmentRequest.class);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> useCase.execute(request, studentId))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("tanárok");

        verify(userRepository).findById(studentId);
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: creates assignment with multiple exercises in order")
    void execute_multipleExercises_orderPreserved() {
        Long teacherId = 4L;
        User teacher = User.builder().id(teacherId).role(UserRole.TEACHER).build();

        CreateAssignmentRequest request = mock(CreateAssignmentRequest.class);
        when(request.getTitle()).thenReturn("Multi Exercise");
        when(request.getDescription()).thenReturn("Test");

        AddExerciseRequest ex1 = mock(AddExerciseRequest.class);
        when(ex1.getType()).thenReturn(ExerciseType.MULTIPLE_CHOICE);
        when(ex1.getOrderIndex()).thenReturn(1);
        when(ex1.getPoints()).thenReturn(5);
        when(ex1.getQuestion()).thenReturn("Q1");

        AddExerciseRequest ex2 = mock(AddExerciseRequest.class);
        when(ex2.getType()).thenReturn(ExerciseType.RADIO);
        when(ex2.getOrderIndex()).thenReturn(2);
        when(ex2.getPoints()).thenReturn(10);
        when(ex2.getQuestion()).thenReturn("Q2");

        when(request.getExercises()).thenReturn(List.of(ex1, ex2));

        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment a = inv.getArgument(0);
            a.setId(200L);
            return a;
        });
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(inv -> inv.getArgument(0));

        Assignment result = useCase.execute(request, teacherId);

        assertThat(result.getId()).isEqualTo(200L);
        verify(exerciseRepository, times(2)).save(any(Exercise.class));
    }

    @Test
    @DisplayName("execute: assignment created with correct timestamps")
    void execute_timestampsSet() {
        Long teacherId = 5L;
        User teacher = User.builder().id(teacherId).role(UserRole.TEACHER).build();

        CreateAssignmentRequest request = mock(CreateAssignmentRequest.class);
        when(request.getTitle()).thenReturn("Timestamp Test");
        when(request.getDescription()).thenReturn("Test");
        when(request.getExercises()).thenReturn(new ArrayList<>());

        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        LocalDateTime beforeExecution = LocalDateTime.now().minusSeconds(1);

        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment assignment = inv.getArgument(0);

            assertThat(assignment.getCreatedAt()).isNotNull();
            assertThat(assignment.getUpdatedAt()).isNotNull();
            assertThat(assignment.getCreatedAt()).isAfter(beforeExecution);
            assertThat(assignment.getUpdatedAt()).isAfter(beforeExecution);

            assignment.setId(300L);
            return assignment;
        });

        Assignment result = useCase.execute(request, teacherId);

        assertThat(result.getId()).isEqualTo(300L);
    }
}
