package hu.kornel.server.application.dto.sandbox;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegexExampleRequestDto {
    @NotBlank(message = "Pattern cannot be empty")
    private String pattern;
    
    private boolean caseInsensitive = false;
    private boolean multiline = false;
}