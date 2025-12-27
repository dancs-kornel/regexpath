package hu.kornel.server.application.usecase.assignments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
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

import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.exception.assignments.AssignmentNotFoundException;
import hu.kornel.server.domain.exception.assignments.MaxAttemptsExceededException;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentGroupAssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;

@ExtendWith(MockitoExtension.class)
class StartAssignmentAttemptUseCaseTest {

    @Mock
    AssignmentRepositoryInterface assignmentRepository;

    @Mock
    AssignmentAttemptRepositoryInterface attemptRepository;

    @Mock
    AssignmentGroupAssignmentRepositoryInterface assignmentGroupAssignmentRepository;

    @Mock
    GroupMembershipRepositoryInterface groupMembershipRepository;

    @InjectMocks
    StartAssignmentAttemptUseCase useCase;

    @Test
    @DisplayName("execute: successful attempt start with time limit")
    void execute_success_withTimeLimit() {
        Long assignmentId = 1L;
        Long groupId = 10L;
        Long userId = 100L;

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .teacherId(1L)
                .timeLimitMinutes(30)
                .maxAttempts(3)
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentGroupAssignmentRepository.existsByAssignmentIdAndGroupId(assignmentId, groupId)).thenReturn(true);
        when(groupMembershipRepository.existsByGroupIdAndStudentId(groupId, userId)).thenReturn(true);
        when(attemptRepository.countByAssignmentIdAndStudentId(assignmentId, userId)).thenReturn(0);
        when(attemptRepository.findByAssignmentIdAndStudentId(assignmentId, userId)).thenReturn(new ArrayList<>());
        when(attemptRepository.save(any(AssignmentAttempt.class))).thenAnswer(inv -> {
            AssignmentAttempt attempt = inv.getArgument(0);
            attempt.setId(1000L);
            return attempt;
        });

        AssignmentAttempt result = useCase.execute(assignmentId, groupId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1000L);
        assertThat(result.getAssignmentId()).isEqualTo(assignmentId);
        assertThat(result.getStudentId()).isEqualTo(userId);
        assertThat(result.getGroupId()).isEqualTo(groupId);
        assertThat(result.getAttemptNumber()).isEqualTo(1);
        assertThat(result.getMaxScore()).isEqualTo(0); // Empty assignment has 0 points
        assertThat(result.isCompleted()).isFalse();
        assertThat(result.getExpiresAt()).isNotNull();
        assertThat(result.getExpiresAt()).isAfter(LocalDateTime.now());

        verify(assignmentRepository).findById(assignmentId);
        verify(attemptRepository).save(any(AssignmentAttempt.class));
    }

    @Test
    @DisplayName("execute: assignment not found throws AssignmentNotFoundException")
    void execute_assignmentNotFound_throws() {
        Long assignmentId = 999L;
        Long groupId = 10L;
        Long userId = 100L;

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(assignmentId, groupId, userId))
                .isInstanceOf(AssignmentNotFoundException.class);

        verify(assignmentRepository).findById(assignmentId);
        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: assignment not assigned to group throws IllegalStateException")
    void execute_assignmentNotAssignedToGroup_throws() {
        Long assignmentId = 1L;
        Long groupId = 10L;
        Long userId = 100L;

        Assignment assignment = Assignment.builder().id(assignmentId).build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentGroupAssignmentRepository.existsByAssignmentIdAndGroupId(assignmentId, groupId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(assignmentId, groupId, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nincs hozzárendelve");

        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: user not member of group throws IllegalStateException")
    void execute_userNotMember_throws() {
        Long assignmentId = 1L;
        Long groupId = 10L;
        Long userId = 100L;

        Assignment assignment = Assignment.builder().id(assignmentId).build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentGroupAssignmentRepository.existsByAssignmentIdAndGroupId(assignmentId, groupId)).thenReturn(true);
        when(groupMembershipRepository.existsByGroupIdAndStudentId(groupId, userId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(assignmentId, groupId, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tagja");

        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: expired assignment throws IllegalStateException")
    void execute_expiredAssignment_throws() {
        Long assignmentId = 1L;
        Long groupId = 10L;
        Long userId = 100L;

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .dueDate(LocalDateTime.now().minusDays(1))
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentGroupAssignmentRepository.existsByAssignmentIdAndGroupId(assignmentId, groupId)).thenReturn(true);
        when(groupMembershipRepository.existsByGroupIdAndStudentId(groupId, userId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(assignmentId, groupId, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lejárt");

        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: max attempts exceeded throws MaxAttemptsExceededException")
    void execute_maxAttemptsExceeded_throws() {
        Long assignmentId = 1L;
        Long groupId = 10L;
        Long userId = 100L;

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .maxAttempts(2)
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentGroupAssignmentRepository.existsByAssignmentIdAndGroupId(assignmentId, groupId)).thenReturn(true);
        when(groupMembershipRepository.existsByGroupIdAndStudentId(groupId, userId)).thenReturn(true);
        when(attemptRepository.countByAssignmentIdAndStudentId(assignmentId, userId)).thenReturn(2);

        assertThatThrownBy(() -> useCase.execute(assignmentId, groupId, userId))
                .isInstanceOf(MaxAttemptsExceededException.class);

        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: active attempt in progress throws IllegalStateException")
    void execute_activeAttemptExists_throws() {
        Long assignmentId = 1L;
        Long groupId = 10L;
        Long userId = 100L;

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .maxAttempts(3)
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

        AssignmentAttempt existingAttempt = AssignmentAttempt.builder()
                .id(500L)
                .completed(false)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentGroupAssignmentRepository.existsByAssignmentIdAndGroupId(assignmentId, groupId)).thenReturn(true);
        when(groupMembershipRepository.existsByGroupIdAndStudentId(groupId, userId)).thenReturn(true);
        when(attemptRepository.countByAssignmentIdAndStudentId(assignmentId, userId)).thenReturn(1);
        when(attemptRepository.findByAssignmentIdAndStudentId(assignmentId, userId))
                .thenReturn(List.of(existingAttempt));

        assertThatThrownBy(() -> useCase.execute(assignmentId, groupId, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("folyamatban");

        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: second attempt starts successfully after first completed")
    void execute_secondAttempt_success() {
        Long assignmentId = 1L;
        Long groupId = 10L;
        Long userId = 100L;

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .maxAttempts(3)
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();

        AssignmentAttempt completedAttempt = AssignmentAttempt.builder()
                .id(500L)
                .completed(true)
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentGroupAssignmentRepository.existsByAssignmentIdAndGroupId(assignmentId, groupId)).thenReturn(true);
        when(groupMembershipRepository.existsByGroupIdAndStudentId(groupId, userId)).thenReturn(true);
        when(attemptRepository.countByAssignmentIdAndStudentId(assignmentId, userId)).thenReturn(1);
        when(attemptRepository.findByAssignmentIdAndStudentId(assignmentId, userId))
                .thenReturn(List.of(completedAttempt));
        when(attemptRepository.save(any(AssignmentAttempt.class))).thenAnswer(inv -> {
            AssignmentAttempt attempt = inv.getArgument(0);
            attempt.setId(2000L);
            return attempt;
        });

        AssignmentAttempt result = useCase.execute(assignmentId, groupId, userId);

        assertThat(result.getId()).isEqualTo(2000L);
        assertThat(result.getAttemptNumber()).isEqualTo(2);

        verify(attemptRepository).save(any(AssignmentAttempt.class));
    }
}
