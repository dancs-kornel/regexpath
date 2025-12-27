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
public class User {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean active;
    private DifficultyLevel difficultyLevel;
    private LocalDateTime difficultyChangedAt;
    private LocalDateTime difficultyPromptShownAt;

    public boolean isTeacher() { return role == UserRole.TEACHER; }
    public boolean isStudent() { return role == UserRole.STUDENT; }
    public boolean isGuest() { return role == UserRole.GUEST; }
    public void updateLastLogin() { this.lastLogin = LocalDateTime.now(); }
    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    public void updateDifficultyLevel(DifficultyLevel newLevel) {
        this.difficultyLevel = newLevel;
        this.difficultyChangedAt = LocalDateTime.now();
    }

    public void recordDifficultyPromptShown() {
        this.difficultyPromptShownAt = LocalDateTime.now();
    }

    public DifficultyLevel getDifficultyLevelOrDefault() {
        return difficultyLevel != null ? difficultyLevel : DifficultyLevel.MEDIUM;
    }
}
