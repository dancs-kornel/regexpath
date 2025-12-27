package hu.kornel.server.application.dto.validation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChoiceValidationResponseDto.class, name = "choice"),
    @JsonSubTypes.Type(value = RegexValidationResponseDto.class, name = "regex_sandbox"),
    @JsonSubTypes.Type(value = XPathValidationResponseDto.class, name = "xpath_sandbox")
})
public abstract class ValidationResponseDto {
    private boolean correct;
    private String message;
    private String explanation;
}
