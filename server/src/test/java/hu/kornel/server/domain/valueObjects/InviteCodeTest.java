package hu.kornel.server.domain.valueObjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class InviteCodeTest {

    @Test
    @DisplayName("Valid invite code is accepted and upper-cased")
    void validCodeAccepted() {
        InviteCode code = InviteCode.of("ABC123");
        assertThat(code.getValue()).isEqualTo("ABC123");
        assertThat(code).hasToString("ABC123");
    }

    @Test
    @DisplayName("Rejects null, empty, or blank codes")
    void rejectsNullOrBlank() {
        assertThatThrownBy(() -> InviteCode.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty or null");

        assertThatThrownBy(() -> InviteCode.of(""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> InviteCode.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Rejects codes of invalid length or format")
    void rejectsInvalidFormat() {
        assertThatThrownBy(() -> InviteCode.of("ABC12")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InviteCode.of("ABCDEFG")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InviteCode.of("ABC!23")).isInstanceOf(IllegalArgumentException.class);
    }

    @RepeatedTest(5)
    @DisplayName("Generate() creates unique 6-character uppercase alphanumeric codes")
    void generateCreatesValidCodes() {
        InviteCode code = InviteCode.generate();
        assertThat(code.getValue())
                .matches("^[A-Z0-9]{6}$");

        InviteCode another = InviteCode.generate();
        assertThat(another.getValue()).hasSize(6);
        assertThat(code).isNotEqualTo(another);
    }

    @Test
    @DisplayName("Equality, hashCode, and toString behave consistently")
    void equalityAndHashCode() {
        InviteCode a = InviteCode.of("ABC123");
        InviteCode b = InviteCode.of("ABC123");
        InviteCode c = InviteCode.of("ZZZ999");

        assertThat(a).isEqualTo(b)
                     .hasSameHashCodeAs(b)
                     .isNotEqualTo(c);
        assertThat(a).hasToString("ABC123");
    }
}
