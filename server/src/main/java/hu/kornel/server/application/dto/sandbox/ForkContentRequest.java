package hu.kornel.server.application.dto.sandbox;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForkContentRequest {
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String name;
    private String description; 
}