package hu.kornel.server.application.dto.validation;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class RegexValidationResponseDto extends ValidationResponseDto {
    private List<TestCaseResultDto> testCaseResults;
    private int passedTests;
    private int totalTests;
    private String compiledPattern;
}
