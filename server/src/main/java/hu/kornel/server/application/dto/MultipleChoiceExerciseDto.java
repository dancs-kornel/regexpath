package hu.kornel.server.application.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class MultipleChoiceExerciseDto extends ExerciseDto {
    private List<OptionDto> options;
}
