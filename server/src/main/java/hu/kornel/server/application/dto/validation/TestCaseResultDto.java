package hu.kornel.server.application.dto.validation;

import java.util.List;

import lombok.Data;

@Data
public class TestCaseResultDto {
    private String description;
    private boolean passed;
    private boolean isPositiveCase;
    private List<String> actualMatches;
    private List<String> expectedMatches;
    private String errorMessage;
}
