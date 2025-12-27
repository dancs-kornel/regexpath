package hu.kornel.server.application.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RegexTestCaseDto {
    private String text;
    private String description;
    
    @JsonProperty("expected_matches")
    private List<String> expectedMatches;
}
