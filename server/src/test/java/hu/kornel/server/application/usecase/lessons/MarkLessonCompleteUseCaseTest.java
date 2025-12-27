package hu.kornel.server.application.usecase.lessons;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.kornel.server.application.dto.LessonDto;
import hu.kornel.server.application.dto.lessons.LessonProgressResponse;
import hu.kornel.server.domain.entities.LessonProgress;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.UserRole;
import hu.kornel.server.domain.exception.LessonNotFoundException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.LessonProgressRepositoryInterface;
import hu.kornel.server.domain.repository.LessonRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;

@ExtendWith(MockitoExtension.class)
class MarkLessonCompleteUseCaseTest {

    @Mock
    LessonProgressRepositoryInterface lessonProgressRepository;

    @Mock
    LessonRepositoryInterface lessonRepository;

    @Mock
    UserRepositoryInterface userRepository;

    @InjectMocks
    MarkLessonCompleteUseCase useCase;

    @Test
    @DisplayName("execute: marks existing lesson progress as completed")
    void execute_existingProgress_marksCompleted() {
        Long userId = 1L;
        String lessonId = "lesson-regex-basics";

        User user = User.builder()
                .id(userId)
                .username("student1")
                .role(UserRole.STUDENT)
                .build();

        LessonDto lesson = new LessonDto();
        lesson.setId(lessonId);
        lesson.setTitle("Regex Basics");

        LessonProgress existingProgress = LessonProgress.builder()
                .userId(userId)
                .lessonId(lessonId)
                .completed(false)
                .lastAccessedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonRepository.getLessonById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId))
                .thenReturn(Optional.of(existingProgress));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(inv -> {
            LessonProgress saved = inv.getArgument(0);
            assertThat(saved.isCompleted()).isTrue();
            assertThat(saved.getCompletedAt()).isNotNull();
            return saved;
        });

        LessonProgressResponse result = useCase.execute(lessonId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getLessonId()).isEqualTo(lessonId);
        assertThat(result.getLessonTitle()).isEqualTo("Regex Basics");
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getLastAccessedAt()).isNotNull();

        verify(userRepository).findById(userId);
        verify(lessonRepository).getLessonById(lessonId);
        verify(lessonProgressRepository).findByUserIdAndLessonId(userId, lessonId);
        verify(lessonProgressRepository).save(any(LessonProgress.class));
    }

    @Test
    @DisplayName("execute: creates new progress when none exists")
    void execute_noExistingProgress_createsNew() {
        Long userId = 2L;
        String lessonId = "lesson-xpath-intro";

        User user = User.builder()
                .id(userId)
                .username("student2")
                .role(UserRole.STUDENT)
                .build();

        LessonDto lesson = new LessonDto();
        lesson.setId(lessonId);
        lesson.setTitle("XPath Introduction");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonRepository.getLessonById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId))
                .thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(inv -> {
            LessonProgress saved = inv.getArgument(0);
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getLessonId()).isEqualTo(lessonId);
            assertThat(saved.isCompleted()).isTrue();
            return saved;
        });

        LessonProgressResponse result = useCase.execute(lessonId, userId);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getLessonId()).isEqualTo(lessonId);

        verify(lessonProgressRepository).save(any(LessonProgress.class));
    }

    @Test
    @DisplayName("execute: user not found throws UserNotFoundException")
    void execute_userNotFound_throws() {
        Long userId = 999L;
        String lessonId = "lesson-test";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(lessonId, userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");

        verify(userRepository).findById(userId);
        verify(lessonRepository, never()).getLessonById(any());
        verify(lessonProgressRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: lesson not found throws LessonNotFoundException")
    void execute_lessonNotFound_throws() {
        Long userId = 3L;
        String lessonId = "non-existent-lesson";

        User user = User.builder()
                .id(userId)
                .username("student3")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonRepository.getLessonById(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(lessonId, userId))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessageContaining("not found");

        verify(userRepository).findById(userId);
        verify(lessonRepository).getLessonById(lessonId);
        verify(lessonProgressRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute: updates lastAccessedAt when marking complete")
    void execute_updatesLastAccessedAt() {
        Long userId = 4L;
        String lessonId = "lesson-patterns";

        User user = User.builder().id(userId).username("student4").build();

        LessonDto lesson = new LessonDto();
        lesson.setId(lessonId);
        lesson.setTitle("Advanced Patterns");

        LocalDateTime oldAccessTime = LocalDateTime.now().minusHours(5);
        LessonProgress existingProgress = LessonProgress.builder()
                .userId(userId)
                .lessonId(lessonId)
                .completed(false)
                .lastAccessedAt(oldAccessTime)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonRepository.getLessonById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId))
                .thenReturn(Optional.of(existingProgress));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(inv -> {
            LessonProgress saved = inv.getArgument(0);
            assertThat(saved.getLastAccessedAt()).isAfter(oldAccessTime);
            return saved;
        });

        LessonProgressResponse result = useCase.execute(lessonId, userId);

        assertThat(result.getLastAccessedAt()).isAfter(oldAccessTime);

        verify(lessonProgressRepository).save(any(LessonProgress.class));
    }

    @Test
    @DisplayName("execute: already completed lesson can be marked complete again")
    void execute_alreadyCompleted_updatesAgain() {
        Long userId = 5L;
        String lessonId = "lesson-completed";

        User user = User.builder().id(userId).username("student5").build();

        LessonDto lesson = new LessonDto();
        lesson.setId(lessonId);
        lesson.setTitle("Completed Lesson");

        LocalDateTime firstCompletionTime = LocalDateTime.now().minusDays(2);
        LessonProgress completedProgress = LessonProgress.builder()
                .userId(userId)
                .lessonId(lessonId)
                .completed(true)
                .completedAt(firstCompletionTime)
                .lastAccessedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lessonRepository.getLessonById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId))
                .thenReturn(Optional.of(completedProgress));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LessonProgressResponse result = useCase.execute(lessonId, userId);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getCompletedAt()).isNotNull();

        verify(lessonProgressRepository).save(any(LessonProgress.class));
    }
}
