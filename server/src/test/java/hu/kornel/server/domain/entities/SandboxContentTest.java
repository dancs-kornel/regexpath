package hu.kornel.server.domain.entities;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SandboxContentTest {

    @Test
    @DisplayName("isOwnedBy returns true when userId matches ownerId")
    void isOwnedBy_returnsTrue() {
        SandboxContent content = SandboxContent.builder()
                .id(1L)
                .ownerId(100L)
                .build();

        assertThat(content.isOwnedBy(100L)).isTrue();
        assertThat(content.isOwnedBy(200L)).isFalse();
    }

    @Test
    @DisplayName("isBuiltIn returns true only for built-in source")
    void isBuiltIn_returnsTrueOnlyForBuiltin() {
        SandboxContent builtIn = SandboxContent.builder()
                .source(ContentSource.BUILTIN)
                .build();

        SandboxContent userUploaded = SandboxContent.builder()
                .source(ContentSource.USER_UPLOADED)
                .build();

        assertThat(builtIn.isBuiltIn()).isTrue();
        assertThat(userUploaded.isBuiltIn()).isFalse();
    }

    @Test
    @DisplayName("canBeEditedBy returns true only for owner and non-built-in content")
    void canBeEditedBy_returnsTrueForOwner() {
        SandboxContent editable = SandboxContent.builder()
                .source(ContentSource.USER_UPLOADED)
                .ownerId(100L)
                .build();

        SandboxContent builtIn = SandboxContent.builder()
                .source(ContentSource.BUILTIN)
                .ownerId(100L)
                .build();

        assertThat(editable.canBeEditedBy(100L)).isTrue();
        assertThat(editable.canBeEditedBy(200L)).isFalse();
        assertThat(builtIn.canBeEditedBy(100L)).isFalse();
    }

    @Test
    @DisplayName("canBeDeletedBy returns true only for owner and non-built-in content")
    void canBeDeletedBy_returnsTrueForOwner() {
        SandboxContent deletable = SandboxContent.builder()
                .source(ContentSource.USER_EDITED)
                .ownerId(100L)
                .build();

        SandboxContent builtIn = SandboxContent.builder()
                .source(ContentSource.BUILTIN)
                .ownerId(100L)
                .build();

        assertThat(deletable.canBeDeletedBy(100L)).isTrue();
        assertThat(deletable.canBeDeletedBy(200L)).isFalse();
        assertThat(builtIn.canBeDeletedBy(100L)).isFalse();
    }

    @Test
    @DisplayName("createFork creates a new editable copy for a user")
    void createFork_createsUserCopy() {
        SandboxContent builtIn = SandboxContent.builder()
                .id(1L)
                .type(ContentType.REGEX_TEXT)
                .name("Email Pattern")
                .description("Validates emails")
                .content("[a-z]+@[a-z]+\\.com")
                .source(ContentSource.BUILTIN)
                .category("regex")
                .difficultyLevel(2)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        SandboxContent fork = builtIn.createFork(200L);

        assertThat(fork.getSource()).isEqualTo(ContentSource.USER_EDITED);
        assertThat(fork.getOwnerId()).isEqualTo(200L);
        assertThat(fork.getOriginalContentId()).isEqualTo(builtIn.getId());
        assertThat(fork.getName()).contains("edited");
        assertThat(fork.getCreatedAt()).isNotNull();
        assertThat(fork.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("createFork throws when content is not built-in")
    void createFork_throwsIfNotBuiltIn() {
        SandboxContent uploaded = SandboxContent.builder()
                .source(ContentSource.USER_UPLOADED)
                .build();

        assertThatThrownBy(() -> uploaded.createFork(123L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only fork built-in content");
    }
}
