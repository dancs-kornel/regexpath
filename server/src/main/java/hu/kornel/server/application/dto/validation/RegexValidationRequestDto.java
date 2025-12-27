package hu.kornel.server.application.dto.validation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class RegexValidationRequestDto extends ValidationRequestDto {
    @NotBlank(message="Regex pattern cannot be empty")
    private String pattern;
}
