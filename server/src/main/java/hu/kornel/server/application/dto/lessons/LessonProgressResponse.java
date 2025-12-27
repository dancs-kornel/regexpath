package hu.kornel.server.application.dto.lessons;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressResponse {
    private String lessonId;
    private String lessonTitle;
    private boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
}