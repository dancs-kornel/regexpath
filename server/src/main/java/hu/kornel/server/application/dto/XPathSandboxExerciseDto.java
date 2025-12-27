package hu.kornel.server.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class XPathSandboxExerciseDto extends ExerciseDto{
    @JsonProperty("sample_document")
    private String sampleDocument;
    private String solution;

    @JsonProperty("enable_real_time_highlighting")
    private Boolean enableRealTimeHighlighting;
}
