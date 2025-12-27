package hu.kornel.server.application.dto.sandbox;

import hu.kornel.server.domain.entities.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadContentRequest {
    
    @NotNull(message = "Content type is required")
    private ContentType type;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    private String category;
}