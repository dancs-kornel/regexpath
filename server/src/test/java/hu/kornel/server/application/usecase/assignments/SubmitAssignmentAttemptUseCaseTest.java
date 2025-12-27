package hu.kornel.server.application.usecase.assignments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.kornel.server.application.dto.ExerciseDto;
import hu.kornel.server.application.dto.MultipleChoiceExerciseDto;
import hu.kornel.server.application.dto.assignments.ExerciseAnswerRequest;
import hu.kornel.server.application.dto.validation.ChoiceValidationRequestDto;
import hu.kornel.server.application.dto.validation.ChoiceValidationResponseDto;
import hu.kornel.server.application.dto.validation.ValidationResponseDto;
import hu.kornel.server.application.service.ExerciseDtoConverter;
import hu.kornel.server.application.service.ExerciseValidationService;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.entities.assignments.ExerciseAnswer;
import hu.kornel.server.domain.entities.assignments.ExerciseType;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseAnswerRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseRepositoryInterface;

@ExtendWith(MockitoExtension.class)
class SubmitAssignmentAttemptUseCaseTest {

    @Mock
    AssignmentAttemptRepositoryInterface attemptRepository;

    @Mock
    ExerciseRepositoryInterface exerciseRepository;

    @Mock
    ExerciseAnswerRepositoryInterface answerRepository;

    @Mock
    ExerciseValidationService validationService;

    @Mock
    ExerciseDtoConverter exerciseDtoConverter;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    SubmitAssignmentAttemptUseCase useCase;

    @Test
    @DisplayName("execute: successful submission with correct answer")
    void execute_success_correctAnswer() throws Exception {
        Long attemptId = 1L;
        Long userId = 100L;
        Long assignmentId = 10L;

        AssignmentAttempt attempt = AssignmentAttempt.builder()
                .id(attemptId)
                .assignmentId(assignmentId)
                .studentId(userId)
                .completed(false)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .maxScore(10)
                .build();

        Exercise exercise = Exercise.builder()
                .id(1L)
                .assignmentId(assignmentId)
                .type(ExerciseType.MULTIPLE_CHOICE)
                .points(10)
                .orderIndex(1)
                .build();

        ExerciseAnswerRequest answerRequest = mock(ExerciseAnswerRequest.class);
        when(answerRequest.getExerciseId()).thenReturn(1L);
        when(answerRequest.getAnswerJson()).thenReturn("{\"selectedOptions\":[\"opt1\"]}");

        MultipleChoiceExerciseDto exerciseDto = new MultipleChoiceExerciseDto();
        exerciseDto.setId("1");

        ChoiceValidationResponseDto validationResponse = new ChoiceValidationResponseDto();
        validationResponse.setCorrect(true);

        com.fasterxml.jackson.databind.JsonNode mockNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode selectedOptionsNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.type.TypeFactory mockTypeFactory = mock(com.fasterxml.jackson.databind.type.TypeFactory.class);
        com.fasterxml.jackson.databind.type.CollectionType mockCollectionType = mock(com.fasterxml.jackson.databind.type.CollectionType.class);

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(exerciseRepository.findByAssignmentIdOrderByOrderIndex(assignmentId))
                .thenReturn(List.of(exercise));
        when(exerciseDtoConverter.toDto(exercise)).thenReturn(exerciseDto);
        when(objectMapper.readTree(any(String.class))).thenReturn(mockNode);
        when(mockNode.get("selectedOptions")).thenReturn(selectedOptionsNode);
        when(objectMapper.getTypeFactory()).thenReturn(mockTypeFactory);
        when(mockTypeFactory.constructCollectionType(any(Class.class), any(Class.class))).thenReturn(mockCollectionType);
        when(objectMapper.convertValue(eq(selectedOptionsNode), eq(mockCollectionType)))
                .thenReturn(List.of("opt1"));
        when(validationService.validateExercise(any(ExerciseDto.class), any()))
                .thenReturn(validationResponse);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(answerRepository.save(any(ExerciseAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attemptRepository.save(any(AssignmentAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignmentAttempt result = useCase.execute(attemptId, List.of(answerRequest), userId);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getScore()).isEqualTo(10);
        assertThat(result.getSubmittedAt()).isNotNull();

        verify(attemptRepository).findById(attemptId);
        verify(exerciseRepository).findByAssignmentIdOrderByOrderIndex(assignmentId);
        verify(answerRepository).save(any(ExerciseAnswer.class));
        verify(attemptRepository).save(any(AssignmentAttempt.class));
    }

    @Test
    @DisplayName("execute: submission with incorrect answer scores zero")
    void execute_incorrectAnswer() throws Exception {
        Long attemptId = 2L;
        Long userId = 100L;
        Long assignmentId = 20L;

        AssignmentAttempt attempt = AssignmentAttempt.builder()
                .id(attemptId)
                .assignmentId(assignmentId)
                .studentId(userId)
                .completed(false)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .maxScore(10)
                .build();

        Exercise exercise = Exercise.builder()
                .id(2L)
                .assignmentId(assignmentId)
                .type(ExerciseType.RADIO)
                .points(10)
                .build();

        ExerciseAnswerRequest answerRequest = mock(ExerciseAnswerRequest.class);
        when(answerRequest.getExerciseId()).thenReturn(2L);
        when(answerRequest.getAnswerJson()).thenReturn("{\"selectedOptions\":[\"wrong\"]}");

        ExerciseDto exerciseDto = mock(ExerciseDto.class);
        ValidationResponseDto validationResponse = mock(ValidationResponseDto.class);
        when(validationResponse.isCorrect()).thenReturn(false);

        com.fasterxml.jackson.databind.JsonNode mockNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode selectedOptionsNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.type.TypeFactory mockTypeFactory = mock(com.fasterxml.jackson.databind.type.TypeFactory.class);
        com.fasterxml.jackson.databind.type.CollectionType mockCollectionType = mock(com.fasterxml.jackson.databind.type.CollectionType.class);

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(exerciseRepository.findByAssignmentIdOrderByOrderIndex(assignmentId))
                .thenReturn(List.of(exercise));
        when(exerciseDtoConverter.toDto(exercise)).thenReturn(exerciseDto);
        when(objectMapper.readTree(any(String.class))).thenReturn(mockNode);
        when(mockNode.get("selectedOptions")).thenReturn(selectedOptionsNode);
        when(objectMapper.getTypeFactory()).thenReturn(mockTypeFactory);
        when(mockTypeFactory.constructCollectionType(any(Class.class), any(Class.class))).thenReturn(mockCollectionType);
        when(objectMapper.convertValue(eq(selectedOptionsNode), eq(mockCollectionType)))
                .thenReturn(List.of("wrong"));
        when(validationService.validateExercise(any(), any())).thenReturn(validationResponse);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(answerRepository.save(any(ExerciseAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attemptRepository.save(any(AssignmentAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignmentAttempt result = useCase.execute(attemptId, List.of(answerRequest), userId);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getScore()).isZero();

        verify(answerRepository).save(any(ExerciseAnswer.class));
    }

    @Test
    @DisplayName("execute: attempt not found throws IllegalStateException")
    void execute_attemptNotFound_throws() {
        Long attemptId = 999L;
        Long userId = 100L;

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(attemptId, new ArrayList<>(), userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nem található");

        verify(attemptRepository).findById(attemptId);
        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: wrong user throws IllegalStateException")
    void execute_wrongUser_throws() {
        Long attemptId = 1L;
        Long attemptOwnerId = 100L;
        Long differentUserId = 200L;

        AssignmentAttempt attempt = AssignmentAttempt.builder()
                .id(attemptId)
                .studentId(attemptOwnerId)
                .build();

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> useCase.execute(attemptId, new ArrayList<>(), differentUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("saját");

        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: already completed attempt throws IllegalStateException")
    void execute_alreadyCompleted_throws() {
        Long attemptId = 1L;
        Long userId = 100L;

        AssignmentAttempt attempt = AssignmentAttempt.builder()
                .id(attemptId)
                .studentId(userId)
                .completed(true)
                .build();

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> useCase.execute(attemptId, new ArrayList<>(), userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("be lett küldve");

        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: expired attempt throws IllegalStateException")
    void execute_expired_throws() {
        Long attemptId = 1L;
        Long userId = 100L;

        AssignmentAttempt attempt = AssignmentAttempt.builder()
                .id(attemptId)
                .studentId(userId)
                .completed(false)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> useCase.execute(attemptId, new ArrayList<>(), userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lejárt");

        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: submission with multiple exercises calculates total score")
    void execute_multipleExercises_totalScore() throws Exception {
        Long attemptId = 3L;
        Long userId = 100L;
        Long assignmentId = 30L;

        AssignmentAttempt attempt = AssignmentAttempt.builder()
                .id(attemptId)
                .assignmentId(assignmentId)
                .studentId(userId)
                .completed(false)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .maxScore(30)
                .build();

        Exercise ex1 = Exercise.builder().id(1L).assignmentId(assignmentId)
                .type(ExerciseType.MULTIPLE_CHOICE).points(10).build();
        Exercise ex2 = Exercise.builder().id(2L).assignmentId(assignmentId)
                .type(ExerciseType.RADIO).points(20).build();

        ExerciseAnswerRequest ans1 = mock(ExerciseAnswerRequest.class);
        when(ans1.getExerciseId()).thenReturn(1L);
        when(ans1.getAnswerJson()).thenReturn("{}");

        ExerciseAnswerRequest ans2 = mock(ExerciseAnswerRequest.class);
        when(ans2.getExerciseId()).thenReturn(2L);
        when(ans2.getAnswerJson()).thenReturn("{}");

        ValidationResponseDto val1 = mock(ValidationResponseDto.class);
        when(val1.isCorrect()).thenReturn(true);
        ValidationResponseDto val2 = mock(ValidationResponseDto.class);
        when(val2.isCorrect()).thenReturn(false);

        com.fasterxml.jackson.databind.JsonNode mockNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.JsonNode selectedOptionsNode = mock(com.fasterxml.jackson.databind.JsonNode.class);
        com.fasterxml.jackson.databind.type.TypeFactory mockTypeFactory = mock(com.fasterxml.jackson.databind.type.TypeFactory.class);
        com.fasterxml.jackson.databind.type.CollectionType mockCollectionType = mock(com.fasterxml.jackson.databind.type.CollectionType.class);

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(exerciseRepository.findByAssignmentIdOrderByOrderIndex(assignmentId))
                .thenReturn(List.of(ex1, ex2));
        when(exerciseDtoConverter.toDto(any())).thenReturn(mock(ExerciseDto.class));
        when(objectMapper.readTree(any(String.class))).thenReturn(mockNode);
        when(mockNode.get("selectedOptions")).thenReturn(selectedOptionsNode);
        when(objectMapper.getTypeFactory()).thenReturn(mockTypeFactory);
        when(mockTypeFactory.constructCollectionType(any(Class.class), any(Class.class))).thenReturn(mockCollectionType);
        when(objectMapper.convertValue(eq(selectedOptionsNode), eq(mockCollectionType)))
                .thenReturn(List.of("opt1"));
        when(validationService.validateExercise(any(), any()))
                .thenReturn(val1)
                .thenReturn(val2);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(answerRepository.save(any(ExerciseAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attemptRepository.save(any(AssignmentAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignmentAttempt result = useCase.execute(attemptId, List.of(ans1, ans2), userId);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getScore()).isEqualTo(10); // Only first exercise correct

        verify(answerRepository, times(2)).save(any(ExerciseAnswer.class));
    }
}
