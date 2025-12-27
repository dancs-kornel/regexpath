package hu.kornel.server.application.dto.sandbox;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegexExampleResponseDto {
    private List<RegexExampleDto> positiveExamples = new ArrayList<>();
    private List<RegexExampleDto> negativeExamples = new ArrayList<>();
    private String errorMessage;
    
    public static RegexExampleResponseDto error(String message) {
        RegexExampleResponseDto response = new RegexExampleResponseDto();
        response.setErrorMessage(message);
        return response;
    }
    
    public static RegexExampleResponseDto success(List<RegexExampleDto> positive, List<RegexExampleDto> negative) {
        return new RegexExampleResponseDto(positive, negative, null);
    }
}