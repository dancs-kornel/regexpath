package hu.kornel.server.domain.repository;

import java.util.List;
import java.util.Optional;

import hu.kornel.server.domain.entities.LessonProgress;

public interface LessonProgressRepositoryInterface {
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, String lessonId);
    List<LessonProgress> findAllByUserId(Long userId);
    List<LessonProgress> findCompletedByUserId(Long userId);
    Optional<LessonProgress> findLastAccessedByUserId(Long userId);
    LessonProgress save(LessonProgress lessonProgress);
    boolean existsByUserIdAndLessonId(Long userId, String lessonId);
    void deleteByUserIdAndLessonId(Long userId, String lessonId);
}