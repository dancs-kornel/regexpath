package hu.kornel.server.application.dto.publicDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PublicXPathSandboxExerciseDto extends PublicExerciseDto {
    @JsonProperty("sample_document")
    private String sampleDocument;

    @JsonProperty("enable_real_time_highlighting")
    private Boolean enableRealTimeHighlighting;

    private String solution;
}
