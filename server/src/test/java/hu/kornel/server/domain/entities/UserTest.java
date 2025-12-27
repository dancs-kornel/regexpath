package hu.kornel.server.domain.entities;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("isTeacher, isStudent, and isGuest return correct role booleans")
    void roleChecks_returnCorrectValues() {
        User teacher = User.builder().role(UserRole.TEACHER).build();
        User student = User.builder().role(UserRole.STUDENT).build();
        User guest = User.builder().role(UserRole.GUEST).build();

        assertThat(teacher.isTeacher()).isTrue();
        assertThat(teacher.isStudent()).isFalse();
        assertThat(teacher.isGuest()).isFalse();

        assertThat(student.isStudent()).isTrue();
        assertThat(student.isTeacher()).isFalse();
        assertThat(student.isGuest()).isFalse();

        assertThat(guest.isGuest()).isTrue();
        assertThat(guest.isStudent()).isFalse();
        assertThat(guest.isTeacher()).isFalse();
    }

    @Test
    @DisplayName("updateLastLogin updates lastLogin timestamp")
    void updateLastLogin_updatesTimestamp() throws InterruptedException {
        LocalDateTime oldTime = LocalDateTime.now().minusMinutes(5);

        User user = User.builder()
                .username("testuser")
                .lastLogin(oldTime)
                .build();

        Thread.sleep(10); 
        user.updateLastLogin();

        assertThat(user.getLastLogin()).isAfter(oldTime);
        assertThat(user.getLastLogin()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("activate and deactivate toggle active flag correctly")
    void activateAndDeactivate_toggleActive() {
        User user = User.builder()
                .username("studentuser")
                .active(false)
                .build();

        user.activate();
        assertThat(user.isActive()).isTrue();

        user.deactivate();
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("builder sets fields correctly")
    void builder_setsFieldsCorrectly() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        User user = User.builder()
                .id(1L)
                .username("teacher1")
                .email("teacher@example.com")
                .passwordHash("hashedpassword")
                .role(UserRole.TEACHER)
                .createdAt(createdAt)
                .active(true)
                .build();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("teacher1");
        assertThat(user.getEmail()).isEqualTo("teacher@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashedpassword");
        assertThat(user.getRole()).isEqualTo(UserRole.TEACHER);
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.isActive()).isTrue();
    }
}
