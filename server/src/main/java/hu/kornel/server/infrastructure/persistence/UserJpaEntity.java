package hu.kornel.server.infrastructure.persistence;

import java.time.LocalDateTime;

import hu.kornel.server.domain.entities.DifficultyLevel;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @Column(nullable=false, unique=true, length=50)
    private String username;

    @Column(nullable=false,unique=true, length=100)
    private String email;

    @Column(name="password_hash", nullable=false, length=255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private UserRole role;

    @Column(name="created_at", nullable=false,updatable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Column(name="last_login")
    private LocalDateTime lastLogin;

    @Column(name="is_active", nullable=false)
    private boolean active;
    
    @Enumerated(EnumType.STRING)
    @Column(name="difficulty_level", nullable=false, length=20)
    @Builder.Default
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;
    
    @Column(name="difficulty_changed_at")
    private LocalDateTime difficultyChangedAt;
    
    @Column(name="difficulty_prompt_shown_at")
    private LocalDateTime difficultyPromptShownAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (difficultyLevel == null) {
            difficultyLevel = DifficultyLevel.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public User toDomain() {
        return User.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .passwordHash(this.passwordHash)
                .role(this.role)
                .createdAt(this.createdAt)
                .lastLogin(this.lastLogin)
                .active(this.active)
                .difficultyLevel(this.difficultyLevel)
                .difficultyChangedAt(this.difficultyChangedAt)
                .difficultyPromptShownAt(this.difficultyPromptShownAt)
                .build();
    }
    
    public static UserJpaEntity fromDomain(User user) {
        UserJpaEntity entity = UserJpaEntity.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .active(user.isActive())
                .difficultyLevel(user.getDifficultyLevelOrDefault())
                .difficultyChangedAt(user.getDifficultyChangedAt())
                .difficultyPromptShownAt(user.getDifficultyPromptShownAt())
                .build();
        
        if (user.getId() != null) {
            entity.setId(user.getId());
        }
        
        return entity;
    }
}