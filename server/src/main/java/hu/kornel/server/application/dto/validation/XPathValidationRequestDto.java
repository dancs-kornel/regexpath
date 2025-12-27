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
public class XPathValidationRequestDto extends ValidationRequestDto {
    @NotBlank(message="XPath expression cannot be empty")
    private String expression;
}
