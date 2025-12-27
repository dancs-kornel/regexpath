package hu.kornel.server.application.dto.sandbox;

import hu.kornel.server.domain.entities.ContentSource;
import hu.kornel.server.domain.entities.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxContentDto {
    
    private Long id;
    private ContentType type;
    private String name;
    private String description;
    private String content;
    private ContentSource source;
    private Long ownerId;
    private String category;
    private Integer difficultyLevel;
    private Long originalContentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}