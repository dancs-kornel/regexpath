package hu.kornel.server.application.dto.sandbox;

import hu.kornel.server.domain.entities.SandboxContent;


public class SandboxContentMapper {
    
    
    public static SandboxContentDto toDto(SandboxContent content) {
        if (content == null) {
            return null;
        }
        
        return SandboxContentDto.builder()
                .id(content.getId())
                .type(content.getType())
                .name(content.getName())
                .description(content.getDescription())
                .content(content.getContent())
                .source(content.getSource())
                .ownerId(content.getOwnerId())
                .category(content.getCategory())
                .difficultyLevel(content.getDifficultyLevel())
                .originalContentId(content.getOriginalContentId())
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }
    
    
    public static SandboxContentListDto toListDto(SandboxContent content) {
        if (content == null) {
            return null;
        }
        
        return SandboxContentListDto.builder()
                .id(content.getId())
                .type(content.getType())
                .name(content.getName())
                .description(content.getDescription())
                .source(content.getSource())
                .category(content.getCategory())
                .difficultyLevel(content.getDifficultyLevel())
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .contentLength(content.getContent() != null ? content.getContent().length() : 0)
                .build();
    }
    
    
    public static SandboxContent toDomain(SandboxContentDto dto) {
        if (dto == null) {
            return null;
        }
        
        return SandboxContent.builder()
                .id(dto.getId())
                .type(dto.getType())
                .name(dto.getName())
                .description(dto.getDescription())
                .content(dto.getContent())
                .source(dto.getSource())
                .ownerId(dto.getOwnerId())
                .category(dto.getCategory())
                .difficultyLevel(dto.getDifficultyLevel())
                .originalContentId(dto.getOriginalContentId())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}