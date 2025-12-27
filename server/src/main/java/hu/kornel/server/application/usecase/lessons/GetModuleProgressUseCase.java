package hu.kornel.server.application.usecase.lessons;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.LessonDto;
import hu.kornel.server.application.dto.ModuleDto;
import hu.kornel.server.application.dto.lessons.LessonProgressResponse;
import hu.kornel.server.application.dto.lessons.ModuleProgressResponse;
import hu.kornel.server.domain.entities.LessonProgress;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.LessonProgressRepositoryInterface;
import hu.kornel.server.domain.repository.LessonRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetModuleProgressUseCase {
    private final LessonProgressRepositoryInterface lessonProgressRepository;
    private final LessonRepositoryInterface lessonRepository;
    private final UserRepositoryInterface userRepository;

    @Transactional(readOnly = true)
    public List<ModuleProgressResponse> execute(Long userId) {
        
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        
        List<LessonProgress> progressList = lessonProgressRepository.findAllByUserId(userId);
        Map<String, LessonProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(LessonProgress::getLessonId, p -> p));

        
        List<ModuleDto> modules = lessonRepository.getAllModules();

        return modules.stream()
                .map(module -> {
                    List<LessonDto> lessons = lessonRepository.getLessonsByModuleId(module.getId());
                    
                    List<LessonProgressResponse> lessonProgressList = lessons.stream()
                            .map(lesson -> {
                                LessonProgress progress = progressMap.get(lesson.getId());
                                return LessonProgressResponse.builder()
                                        .lessonId(lesson.getId())
                                        .lessonTitle(lesson.getTitle())
                                        .completed(progress != null && progress.isCompleted())
                                        .completedAt(progress != null ? progress.getCompletedAt() : null)
                                        .lastAccessedAt(progress != null ? progress.getLastAccessedAt() : null)
                                        .build();
                            })
                            .collect(Collectors.toList());

                    long completedCount = lessonProgressList.stream()
                            .filter(LessonProgressResponse::isCompleted)
                            .count();

                    double percentage = lessons.isEmpty() ? 0.0 : 
                            (completedCount * 100.0) / lessons.size();

                    return ModuleProgressResponse.builder()
                            .moduleId(module.getId())
                            .moduleTitle(module.getTitle())
                            .totalLessons(lessons.size())
                            .completedLessons((int) completedCount)
                            .progressPercentage(Math.round(percentage * 10.0) / 10.0)
                            .lessons(lessonProgressList)
                            .build();
                })
                .collect(Collectors.toList());
    }
}