package hu.kornel.server.application.service;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hu.kornel.server.application.dto.RegexTestCaseDto;
import hu.kornel.server.application.dto.validation.PatternValidationResultDto;
import hu.kornel.server.application.dto.validation.TestCaseResultDto;

class RegexValidationServiceTest {

    private final RegexValidationService service = new RegexValidationService();

    // Syntax Validation 

    @Test
    @DisplayName("validatePatternSyntax returns invalid result for null pattern")
    void validatePatternSyntax_nullPattern_returnsInvalid() {
        PatternValidationResultDto result = service.validatePatternSyntax(null);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("cannot be null");
    }

    @Test
    @DisplayName("validatePatternSyntax returns invalid result for empty pattern")
    void validatePatternSyntax_emptyPattern_returnsInvalid() {
        PatternValidationResultDto result = service.validatePatternSyntax("   ");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("cannot be empty");
    }

    @Test
    @DisplayName("validatePatternSyntax returns invalid result for empty group pattern")
    void validatePatternSyntax_emptyGroup_returnsInvalid() {
        PatternValidationResultDto result = service.validatePatternSyntax("(?:)");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Empty group");
    }

    @Test
    @DisplayName("validatePatternSyntax returns invalid result for malformed pattern")
    void validatePatternSyntax_invalidPattern_returnsInvalid() {
        PatternValidationResultDto result = service.validatePatternSyntax("[A-Z");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Invalid regex syntax");
    }

    @Test
    @DisplayName("validatePatternSyntax returns valid result for correct pattern")
    void validatePatternSyntax_validPattern_returnsValid() {
        PatternValidationResultDto result = service.validatePatternSyntax("[A-Z]+");
        assertThat(result.isValid()).isTrue();
        assertThat(result.getCompiledPattern()).isNotNull();
    }

    // Positive Test Cases 

    @Test
    @DisplayName("validatePositiveTestCases passes for matching cases")
    void validatePositiveTestCases_passesForMatches() {
        Pattern pattern = Pattern.compile("\\d+");
        RegexTestCaseDto case1 = new RegexTestCaseDto("123", "numbers only", List.of("123"));
        RegexTestCaseDto case2 = new RegexTestCaseDto("abc123xyz", "contains digits", List.of("123"));

        List<TestCaseResultDto> results = service.validatePositiveTestCases(pattern, List.of(case1, case2));

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(TestCaseResultDto::isPassed);
        assertThat(results.get(0).getActualMatches()).containsExactly("123");
    }

    @Test
    @DisplayName("validatePositiveTestCases fails when no matches found")
    void validatePositiveTestCases_failsWhenNoMatch() {
        Pattern pattern = Pattern.compile("\\d+");
        RegexTestCaseDto testCase = new RegexTestCaseDto("no digits here", "no match", List.of());

        List<TestCaseResultDto> results = service.validatePositiveTestCases(pattern, List.of(testCase));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).isPassed()).isFalse();
        assertThat(results.get(0).getErrorMessage()).contains("No match found");
    }

    // Negative Test Cases 

    @Test
    @DisplayName("validateNegativeTestCases passes when no matches found")
    void validateNegativeTestCases_passesWhenNoMatch() {
        Pattern pattern = Pattern.compile("\\d+");
        RegexTestCaseDto testCase = new RegexTestCaseDto("hello", "no digits", List.of());

        List<TestCaseResultDto> results = service.validateNegativeTestCases(pattern, List.of(testCase));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).isPassed()).isTrue();
    }

    @Test
    @DisplayName("validateNegativeTestCases fails when unwanted match occurs")
    void validateNegativeTestCases_failsWhenUnwantedMatch() {
        Pattern pattern = Pattern.compile("\\d+");
        RegexTestCaseDto testCase = new RegexTestCaseDto("abc123", "contains digits", List.of());

        List<TestCaseResultDto> results = service.validateNegativeTestCases(pattern, List.of(testCase));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).isPassed()).isFalse();
        assertThat(results.get(0).getErrorMessage()).contains("Pattern matched when it should not have");
    }
}
