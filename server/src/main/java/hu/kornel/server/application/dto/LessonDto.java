package hu.kornel.server.application.dto;

import java.util.List;

import lombok.Data;

@Data
public class LessonDto {
    private String id;
    private String title;
    private String description;
    private String introduction;
    private List<ExerciseDto> exercises;
    private String summary;
}
