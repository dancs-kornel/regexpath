package hu.kornel.server.domain.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxContent {
    
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
    
    public boolean isOwnedBy(Long userId) {
        return this.ownerId != null && this.ownerId.equals(userId);
    }
    
    
    public boolean isBuiltIn() {
        return this.source == ContentSource.BUILTIN;
    }
    
    
    public boolean canBeEditedBy(Long userId) {
        if (isBuiltIn()) {
            return false;
        }
        return isOwnedBy(userId);
    }
    
    
    public boolean canBeDeletedBy(Long userId) {
        return !isBuiltIn() && isOwnedBy(userId);
    }
    
    
    public SandboxContent createFork(Long newOwnerId) {
        if (!isBuiltIn()) {
            throw new IllegalStateException("Can only fork built-in content");
        }
        
        return SandboxContent.builder()
                .type(this.type)
                .name(this.name + " (edited)")
                .description(this.description)
                .content(this.content)
                .source(ContentSource.USER_EDITED)
                .ownerId(newOwnerId)
                .category(this.category)
                .difficultyLevel(this.difficultyLevel)
                .originalContentId(this.id)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}