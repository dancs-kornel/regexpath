package hu.kornel.server.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class RegexSandboxExerciseDto extends ExerciseDto {
    @JsonProperty("test_cases")
    private RegexTestCasesDto testCases;
    private String solution;

    @JsonProperty("enable_real_time_highlighting")
    private Boolean enableRealTimeHighlighting;
}
