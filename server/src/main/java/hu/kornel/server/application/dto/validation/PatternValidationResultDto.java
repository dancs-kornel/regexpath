package hu.kornel.server.application.dto.validation;

import java.util.regex.Pattern;

import lombok.Getter;

@Getter
public class PatternValidationResultDto {
    private final boolean valid;
    private final String errorMessage;
    private final Pattern compiledPattern;

    private PatternValidationResultDto(boolean valid, String errorMessage, Pattern compiledPattern) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.compiledPattern = compiledPattern;
    }

    public static PatternValidationResultDto valid(Pattern compiledPattern) {
        return new PatternValidationResultDto(true, null, compiledPattern);
    }

    public static PatternValidationResultDto invalid(String errorMessage) {
        return new PatternValidationResultDto(false, errorMessage, null);
    }

    public boolean isInvalid() {
        return !valid;
    }
}
