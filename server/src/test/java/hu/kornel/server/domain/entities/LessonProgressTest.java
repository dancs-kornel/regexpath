package hu.kornel.server.domain.entities;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LessonProgressTest {

    @Test
    @DisplayName("markAsCompleted sets completed to true and updates completedAt")
    void markAsCompleted_setsCompletedAndTimestamp() {
        LessonProgress progress = LessonProgress.builder()
                .userId(1L)
                .lessonId("lesson-1")
                .completed(false)
                .build();

        progress.markAsCompleted();

        assertThat(progress.isCompleted()).isTrue();
        assertThat(progress.getCompletedAt()).isNotNull();
        assertThat(progress.getCompletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("updateLastAccessed updates lastAccessedAt timestamp")
    void updateLastAccessed_updatesTimestamp() throws InterruptedException {
        LessonProgress progress = LessonProgress.builder()
                .userId(1L)
                .lessonId("lesson-1")
                .lastAccessedAt(LocalDateTime.now().minusHours(1))
                .build();

        LocalDateTime before = progress.getLastAccessedAt();

        Thread.sleep(10);
        progress.updateLastAccessed();

        assertThat(progress.getLastAccessedAt()).isAfter(before);
        assertThat(progress.getLastAccessedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("isCompleted returns correct completion state")
    void isCompleted_returnsCorrectState() {
        LessonProgress progress = LessonProgress.builder()
                .completed(false)
                .build();

        assertThat(progress.isCompleted()).isFalse();

        progress.markAsCompleted();

        assertThat(progress.isCompleted()).isTrue();
    }
}
