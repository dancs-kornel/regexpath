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
public class ContinueLearningResponse {
    private String lessonId;
    private String lessonTitle;
    private String moduleId;
    private String moduleTitle;
    private LocalDateTime lastAccessedAt;
}