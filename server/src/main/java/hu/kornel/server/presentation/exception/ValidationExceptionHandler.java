package hu.kornel.server.presentation.exception;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import hu.kornel.server.application.dto.validation.ChoiceValidationResponseDto;
import hu.kornel.server.application.dto.validation.RegexValidationResponseDto;
import hu.kornel.server.application.dto.validation.ValidationResponseDto;
import hu.kornel.server.presentation.controller.ExerciseValidationController;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(assignableTypes=ExerciseValidationController.class)
@Slf4j
public class ValidationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationResponseDto> handleValidationException(MethodArgumentNotValidException e) {
        log.debug("Handling validation exception: {}", e.getMessage());
        String errorMessage = e.getBindingResult().getFieldErrors().stream().map(error -> error.getDefaultMessage()).collect(Collectors.joining(", "));
        String objectName = e.getBindingResult().getObjectName();
        boolean isRegexValidation = e.getBindingResult().hasFieldErrors("pattern");
        ValidationResponseDto response;
        if (isRegexValidation) {
            RegexValidationResponseDto regexResponse = new RegexValidationResponseDto();
            regexResponse.setTestCaseResults(new ArrayList<>());
            regexResponse.setPassedTests(0);
            regexResponse.setTotalTests(0);
            Object rejectedValue = e.getBindingResult().getFieldError("pattern") != null ? e.getBindingResult().getFieldError("pattern").getRejectedValue() : null;
            regexResponse.setCompiledPattern(rejectedValue != null ? rejectedValue.toString() : "");
            response = regexResponse;
            log.debug("Created regex validation error response");
        } else {
            ChoiceValidationResponseDto choiceResponse = new ChoiceValidationResponseDto();
            choiceResponse.setCorrectAnswers(new ArrayList<>());
            response = choiceResponse;
            log.debug("Created choice validation error response");
        }
        response.setCorrect(false);
        response.setMessage(errorMessage);
        response.setExplanation("");
        log.debug("Validation error response: {}", errorMessage);
        return ResponseEntity.badRequest().body(response);
    }
}
