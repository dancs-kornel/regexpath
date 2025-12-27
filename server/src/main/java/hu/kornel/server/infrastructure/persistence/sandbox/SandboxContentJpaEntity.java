package hu.kornel.server.infrastructure.persistence.sandbox;

import hu.kornel.server.domain.entities.ContentSource;
import hu.kornel.server.domain.entities.ContentType;
import hu.kornel.server.domain.entities.SandboxContent;
import hu.kornel.server.infrastructure.persistence.UserJpaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "sandbox_content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxContentJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentType type;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentSource source;
    
    @Column(name = "owner_id")
    private Long ownerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private UserJpaEntity owner;
    
    @Column(length = 100)
    private String category;
    
    @Column(name = "difficulty_level")
    private Integer difficultyLevel;
    
    @Column(name = "original_content_id")
    private Long originalContentId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    
    public static SandboxContentJpaEntity fromDomain(SandboxContent content) {
        return SandboxContentJpaEntity.builder()
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
    
    
    public SandboxContent toDomain() {
        return SandboxContent.builder()
                .id(this.id)
                .type(this.type)
                .name(this.name)
                .description(this.description)
                .content(this.content)
                .source(this.source)
                .ownerId(this.ownerId)
                .category(this.category)
                .difficultyLevel(this.difficultyLevel)
                .originalContentId(this.originalContentId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}