package hu.kornel.server.application.dto.validation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

@Data
@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, property="type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChoiceValidationRequestDto.class, name = "choice"),
    @JsonSubTypes.Type(value = RegexValidationRequestDto.class, name = "regex_sandbox"),
    @JsonSubTypes.Type(value = XPathValidationRequestDto.class, name = "xpath_sandbox")

})
public abstract class ValidationRequestDto {

}
