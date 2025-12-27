package hu.kornel.server.application.usecase.lessons;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.LessonDto;
import hu.kornel.server.application.dto.lessons.LessonProgressResponse;
import hu.kornel.server.domain.entities.LessonProgress;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.LessonProgressRepositoryInterface;
import hu.kornel.server.domain.repository.LessonRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetUserLessonProgressUseCase {
    private final LessonProgressRepositoryInterface lessonProgressRepository;
    private final LessonRepositoryInterface lessonRepository;
    private final UserRepositoryInterface userRepository;

    @Transactional(readOnly = true)
    public List<LessonProgressResponse> execute(Long userId) {
        
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        
        List<LessonProgress> progressList = lessonProgressRepository.findAllByUserId(userId);

        
        return progressList.stream()
                .map(progress -> {
                    LessonDto lesson = lessonRepository.getLessonById(progress.getLessonId())
                            .orElse(null);
                    
                    String lessonTitle = lesson != null ? lesson.getTitle() : progress.getLessonId();
                    
                    return LessonProgressResponse.builder()
                            .lessonId(progress.getLessonId())
                            .lessonTitle(lessonTitle)
                            .completed(progress.isCompleted())
                            .completedAt(progress.getCompletedAt())
                            .lastAccessedAt(progress.getLastAccessedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}