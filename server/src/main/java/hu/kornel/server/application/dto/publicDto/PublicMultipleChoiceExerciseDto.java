package hu.kornel.server.application.dto.publicDto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class PublicMultipleChoiceExerciseDto extends PublicExerciseDto {
    private List<PublicOptionDto> options;
}
