package hu.kornel.server.application.dto.sandbox;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegexExampleDto {
    private String text;
    private String reason;

    public RegexExampleDto(String text) {
        this.text = text;
        this.reason = null;
    }
}
