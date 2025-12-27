package hu.kornel.server.application.dto.publicDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import hu.kornel.server.application.dto.RegexTestCasesDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class PublicRegexSandboxExerciseDto extends PublicExerciseDto {
    @JsonProperty("test_cases")
    private RegexTestCasesDto testCases;

    @JsonProperty("enable_real_time_highlighting")
    private Boolean enableRealTimeHighlighting;
}
