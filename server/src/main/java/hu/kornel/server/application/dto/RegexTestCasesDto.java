package hu.kornel.server.application.dto;

import java.util.List;

import lombok.Data;

@Data
public class RegexTestCasesDto {
    private List<RegexTestCaseDto> positive;
    private List<RegexTestCaseDto> negative;
}
