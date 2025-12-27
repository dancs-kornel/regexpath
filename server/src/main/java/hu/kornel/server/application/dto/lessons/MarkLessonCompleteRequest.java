package hu.kornel.server.application.dto.lessons;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkLessonCompleteRequest {
    @NotBlank(message = "Lesson ID is required")
    private String lessonId;
}