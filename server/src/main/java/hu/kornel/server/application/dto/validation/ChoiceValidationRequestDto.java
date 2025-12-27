package hu.kornel.server.application.dto.validation;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class ChoiceValidationRequestDto extends ValidationRequestDto {
    @NotEmpty(message = "At least one option must be selected")
    private List<String> selectedOptions;
}
