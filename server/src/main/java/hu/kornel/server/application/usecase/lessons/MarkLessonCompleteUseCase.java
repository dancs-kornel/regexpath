package hu.kornel.server.application.usecase.lessons;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.lessons.LessonProgressResponse;
import hu.kornel.server.domain.entities.LessonProgress;
import hu.kornel.server.domain.exception.LessonNotFoundException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.LessonProgressRepositoryInterface;
import hu.kornel.server.domain.repository.LessonRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarkLessonCompleteUseCase {
    private final LessonProgressRepositoryInterface lessonProgressRepository;
    private final LessonRepositoryInterface lessonRepository;
    private final UserRepositoryInterface userRepository;

    @Transactional
    public LessonProgressResponse execute(String lessonId, Long userId) {
        
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        
        var lesson = lessonRepository.getLessonById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found with id: " + lessonId));

        
        LessonProgress progress = lessonProgressRepository
                .findByUserIdAndLessonId(userId, lessonId)
                .orElse(LessonProgress.builder()
                        .userId(userId)
                        .lessonId(lessonId)
                        .completed(false)
                        .lastAccessedAt(LocalDateTime.now())
                        .build());

        
        progress.markAsCompleted();
        progress.updateLastAccessed();

        LessonProgress saved = lessonProgressRepository.save(progress);

        return LessonProgressResponse.builder()
                .lessonId(saved.getLessonId())
                .lessonTitle(lesson.getTitle())
                .completed(saved.isCompleted())
                .completedAt(saved.getCompletedAt())
                .lastAccessedAt(saved.getLastAccessedAt())
                .build();
    }
}