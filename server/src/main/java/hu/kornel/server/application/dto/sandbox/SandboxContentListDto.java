package hu.kornel.server.application.dto.sandbox;

import java.time.LocalDateTime;

import hu.kornel.server.domain.entities.ContentSource;
import hu.kornel.server.domain.entities.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxContentListDto {
    
    private Long id;
    private ContentType type;
    private String name;
    private String description;
    private ContentSource source;
    private String category;
    private Integer difficultyLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Integer contentLength;
}