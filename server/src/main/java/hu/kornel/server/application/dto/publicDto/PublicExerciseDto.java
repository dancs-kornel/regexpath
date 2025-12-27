package hu.kornel.server.application.dto.publicDto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

@Data
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value=PublicMultipleChoiceExerciseDto.class, name="multiple_choice"),
    @JsonSubTypes.Type(value=PublicRadioExerciseDto.class, name="radio"),
    @JsonSubTypes.Type(value=PublicRegexSandboxExerciseDto.class, name="regex_sandbox"),
    @JsonSubTypes.Type(value=PublicXPathSandboxExerciseDto.class, name="xpath_sandbox")
})
public abstract class PublicExerciseDto {
    private String id;
    private String type;
    private Integer order;
    private String title;
    private String question;
    private String instruction;
    private List<String> hints;
    private String explanation;
}
