package hu.kornel.server.application.dto.lessons;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleProgressResponse {
    private String moduleId;
    private String moduleTitle;
    private int totalLessons;
    private int completedLessons;
    private double progressPercentage;
    private List<LessonProgressResponse> lessons;
}