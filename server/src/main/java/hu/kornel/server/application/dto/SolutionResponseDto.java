package hu.kornel.server.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SolutionResponseDto {
    private String solution;
    private String exerciseType;
}
