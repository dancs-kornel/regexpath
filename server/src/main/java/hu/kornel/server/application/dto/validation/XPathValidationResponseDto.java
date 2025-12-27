package hu.kornel.server.application.dto.validation;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class XPathValidationResponseDto extends ValidationResponseDto {
    private List<String> expectedNodes;
    private List<String> actualNodes;
    private String compiledExpression;
}
