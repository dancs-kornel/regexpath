package hu.kornel.server.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.stereotype.Service;

import hu.kornel.server.application.dto.RegexTestCaseDto;
import hu.kornel.server.application.dto.validation.PatternValidationResultDto;
import hu.kornel.server.application.dto.validation.TestCaseResultDto;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegexValidationService {
    public PatternValidationResultDto validatePatternSyntax(String pattern) {
        log.debug("Validating regex pattern syntax: {}", pattern);
        if (pattern == null) {
            log.debug("Pattern is null");
            return PatternValidationResultDto.invalid("Pattern cannot be null");
        }

        if (pattern.trim().isEmpty()) {
            log.debug("Pattern is empty");
            return PatternValidationResultDto.invalid("Pattern cannot be empty");
        }

        if ("(?:)".equals(pattern)) {
            log.debug("Empty group pattern");
            return PatternValidationResultDto.invalid("Empty group pattern is not allowed");
        }

        try {
            Pattern compiledPattern = Pattern.compile(pattern);
            log.debug("Pattern compiled successfully: '{}'", pattern);
            return PatternValidationResultDto.valid(compiledPattern);
        } catch (PatternSyntaxException e) {
            log.debug("Pattern syntax error: '{}' - {}", pattern, e.getDescription());
            return PatternValidationResultDto.invalid(String.format("Invalid regex syntax: %s at position %d", e.getDescription(), e.getIndex()));
        }catch (Exception e) {
            log.error("Unexpected error validating pattern: '{}'", pattern, e);
            return PatternValidationResultDto.invalid("Pattern validation failed: "+e.getMessage());
        }
    }

    public List<TestCaseResultDto> validatePositiveTestCases(Pattern pattern, List<RegexTestCaseDto> positiveTestCases) {
        log.debug("Validating {} positive test cases", positiveTestCases.size());
        return validateTestCases(pattern, positiveTestCases, true);
    }

    public List<TestCaseResultDto> validateNegativeTestCases(Pattern pattern, List<RegexTestCaseDto> negativeTestCases) {
        log.debug("Validating {} negative test cases", negativeTestCases.size());
        return validateTestCases(pattern, negativeTestCases, false);
    }

    private List<TestCaseResultDto> validateTestCases(Pattern pattern, List<RegexTestCaseDto> testCases, boolean isPositive) {
        List<TestCaseResultDto> results = new ArrayList<>();
        for (RegexTestCaseDto testCase : testCases) {
            TestCaseResultDto result = createTestCaseResult(testCase, isPositive);
            try {
                Matcher matcher = pattern.matcher(testCase.getText());
                boolean hasMatch = matcher.find();
                boolean shouldPass = isPositive ? hasMatch : !hasMatch;
                if (shouldPass) handlePassingTest(result, matcher, hasMatch, isPositive, testCase.getDescription());
                else handleFailingTest(result, matcher, hasMatch, testCase.getDescription());
            } catch (Exception e) {
                handleTestError(result, testCase.getDescription(), e);
            }
            results.add(result);
        }
        logTestSummary(results, testCases.size(), isPositive);
        return results;
    }

    private TestCaseResultDto createTestCaseResult(RegexTestCaseDto testCase, boolean isPositive) {
        TestCaseResultDto result = new TestCaseResultDto();
        result.setDescription(testCase.getDescription());
        result.setPositiveCase(isPositive);
        result.setExpectedMatches(testCase.getExpectedMatches());
        return result;
    }

    private void handlePassingTest(TestCaseResultDto result, Matcher matcher, boolean hasMatch, boolean isPositive, String description) {
        result.setPassed(true);
        
        if (hasMatch) {
            List<String> actualMatches = collectAllMatches(matcher);
            result.setActualMatches(actualMatches);
            log.debug("{} test case PASSED: '{}' - found {} match(es)", isPositive ? "Positive" : "Negative", description, actualMatches.size());
        } else {
            result.setActualMatches(List.of());
            log.debug("{} test case PASSED: '{}' - no match found (as expected)", isPositive ? "Positive" : "Negative", description);
        }
    }

    private void handleFailingTest(TestCaseResultDto result, Matcher matcher, boolean hasMatch, String description) {
        result.setPassed(false);
        if (hasMatch) {
            List<String> actualMatches = collectAllMatches(matcher);
            result.setActualMatches(actualMatches);
            result.setErrorMessage(String.format("Pattern matched when it should not have. Found %d match(es)", actualMatches.size()));
            log.debug("Negative test case FAILED: '{}' - found {} unwanted match(es)", description, actualMatches.size());
        } else {
            result.setActualMatches(List.of());
            result.setErrorMessage("No match found in text");
            log.debug("Positive test case FAILED: '{}' - no match found", description);
        }
    }

    private void handleTestError(TestCaseResultDto result, String description, Exception e) {
        result.setPassed(false);
        result.setActualMatches(List.of());
        result.setErrorMessage("Error during matching: " + e.getMessage());
        log.error("Error validating test case '{}': {}", description, e.getMessage(), e);
    }

    private List<String> collectAllMatches(Matcher matcher) {
        List<String> matches = new ArrayList<>();
        matcher.reset();
        while (matcher.find()) matches.add(matcher.group());
        return matches;
    }

    private void logTestSummary(List<TestCaseResultDto> results, int totalTests, boolean isPositive) {
        long passedCount = results.stream().filter(TestCaseResultDto::isPassed).count();
        log.debug("{} test cases: {}/{} passed", isPositive ? "Positive" : "Negative", passedCount, totalTests);
    }    
}
