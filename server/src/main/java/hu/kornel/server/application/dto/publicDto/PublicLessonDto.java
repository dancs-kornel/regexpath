package hu.kornel.server.application.dto.publicDto;

import java.util.List;

import lombok.Data;

@Data
public class PublicLessonDto {
    private String id;
    private String title;
    private String description;
    private String introduction;
    private List<PublicExerciseDto> exercises;
    private String summary;
}
