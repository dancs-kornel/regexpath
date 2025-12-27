package hu.kornel.server.presentation.mapper;

import org.springframework.stereotype.Component;

import hu.kornel.server.application.dto.lessons.LessonProgressDto;
import hu.kornel.server.application.dto.lessons.LessonProgressResponse;
import hu.kornel.server.domain.entities.LessonProgress;

@Component
public class LessonProgressMapper {

    public LessonProgressDto toDto(LessonProgress lessonProgress) {
        return LessonProgressDto.builder()
                .id(lessonProgress.getId())
                .userId(lessonProgress.getUserId())
                .lessonId(lessonProgress.getLessonId())
                .completed(lessonProgress.isCompleted())
                .completedAt(lessonProgress.getCompletedAt())
                .lastAccessedAt(lessonProgress.getLastAccessedAt())
                .createdAt(lessonProgress.getCreatedAt())
                .updatedAt(lessonProgress.getUpdatedAt())
                .build();
    }

    public LessonProgress toDomain(LessonProgressDto dto) {
        return LessonProgress.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .lessonId(dto.getLessonId())
                .completed(dto.isCompleted())
                .completedAt(dto.getCompletedAt())
                .lastAccessedAt(dto.getLastAccessedAt())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public LessonProgressResponse toResponse(LessonProgressDto dto, String lessonTitle) {
        return LessonProgressResponse.builder()
                .lessonId(dto.getLessonId())
                .lessonTitle(lessonTitle)
                .completed(dto.isCompleted())
                .completedAt(dto.getCompletedAt())
                .lastAccessedAt(dto.getLastAccessedAt())
                .build();
    }
}