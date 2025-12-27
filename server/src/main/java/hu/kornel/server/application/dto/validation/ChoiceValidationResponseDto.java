package hu.kornel.server.application.dto.validation;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ChoiceValidationResponseDto extends ValidationResponseDto {
    private List<String> correctAnswers;
}
