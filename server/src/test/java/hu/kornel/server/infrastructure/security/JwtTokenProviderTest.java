package hu.kornel.server.infrastructure.security;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.UserRole;
import hu.kornel.server.domain.exception.InvalidTokenException;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        String testSecret = "test-secret-key-for-jwt-token-provider-unit-tests-12345678";
        long testExpiration = 3600000L; // 1 hour in milliseconds

        jwtTokenProvider = new JwtTokenProvider(testSecret, testExpiration);

        testUser = User.builder()
                .id(123L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashed-password")
                .role(UserRole.STUDENT)
                .build();
    }

    @Test
    @DisplayName("generateToken: creates valid token with all claims")
    void generateToken_success() {
        String token = jwtTokenProvider.generateToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        String email = jwtTokenProvider.getEmailFromToken(token);
        String role = jwtTokenProvider.getRoleFromToken(token);
        Date expiration = jwtTokenProvider.getExpirationDateFromToken(token);

        assertThat(userId).isEqualTo(123L);
        assertThat(username).isEqualTo("testuser");
        assertThat(email).isEqualTo("test@example.com");
        assertThat(role).isEqualTo("STUDENT");
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("validateToken: returns true for valid non-expired token")
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.generateToken(testUser);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("getUserIdFromToken: extracts correct user ID from subject claim")
    void getUserIdFromToken_success() {
        String token = jwtTokenProvider.generateToken(testUser);

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(123L);
    }

    @Test
    @DisplayName("getUsernameFromToken: extracts correct username from claims")
    void getUsernameFromToken_success() {
        String token = jwtTokenProvider.generateToken(testUser);

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getEmailFromToken: extracts correct email from claims")
    void getEmailFromToken_success() {
        String token = jwtTokenProvider.generateToken(testUser);

        String email = jwtTokenProvider.getEmailFromToken(token);

        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("getRoleFromToken: extracts correct role from claims")
    void getRoleFromToken_success() {
        String token = jwtTokenProvider.generateToken(testUser);

        String role = jwtTokenProvider.getRoleFromToken(token);

        assertThat(role).isEqualTo("STUDENT");
    }

    @Test
    @DisplayName("extractClaims: expired token throws InvalidTokenException with 'expired' message")
    void extractClaims_expiredToken_throws() {
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(
                "test-secret-key-for-jwt-token-provider-unit-tests-12345678",
                1L // 1 millisecond
        );

        String expiredToken = shortExpirationProvider.generateToken(testUser);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(expiredToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("extractClaims: malformed token throws InvalidTokenException with 'malformed' message")
    void extractClaims_malformedToken_throws() {
        String malformedToken = "this.is.not.a.valid.jwt";

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(malformedToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("malformed");
    }

    @Test
    @DisplayName("extractClaims: token with invalid signature throws InvalidTokenException with 'invalid' message")
    void extractClaims_invalidSignature_throws() {
        JwtTokenProvider differentSecretProvider = new JwtTokenProvider(
                "different-secret-key-for-jwt-token-provider-tests-87654321",
                3600000L
        );
        String tokenWithDifferentSignature = differentSecretProvider.generateToken(testUser);

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(tokenWithDifferentSignature))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    @DisplayName("extractClaims: null or empty token throws InvalidTokenException")
    void extractClaims_nullOrEmptyToken_throws() {
        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(null))
                .isInstanceOf(InvalidTokenException.class);

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(""))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("validateToken: throws InvalidTokenException for expired token")
    void validateToken_expiredToken_throws() {
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(
                "test-secret-key-for-jwt-token-provider-unit-tests-12345678",
                1L
        );

        String expiredToken = shortExpirationProvider.generateToken(testUser);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("validateToken: throws InvalidTokenException for malformed token")
    void validateToken_malformedToken_throws() {
        String malformedToken = "invalid.token.format";

        assertThatThrownBy(() -> jwtTokenProvider.validateToken(malformedToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("malformed");
    }

    @Test
    @DisplayName("validateToken: throws InvalidTokenException for token with invalid signature")
    void validateToken_invalidSignature_throws() {
        JwtTokenProvider differentSecretProvider = new JwtTokenProvider(
                "different-secret-key-for-jwt-token-provider-tests-87654321",
                3600000L
        );
        String tokenWithDifferentSignature = differentSecretProvider.generateToken(testUser);

        assertThatThrownBy(() -> jwtTokenProvider.validateToken(tokenWithDifferentSignature))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    @DisplayName("isTokenExpired: returns true for expired token")
    void isTokenExpired_expiredToken_returnsTrue() {
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(
                "test-secret-key-for-jwt-token-provider-unit-tests-12345678",
                1L
        );

        String expiredToken = shortExpirationProvider.generateToken(testUser);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> jwtTokenProvider.isTokenExpired(expiredToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("isTokenExpired: returns false for valid non-expired token")
    void isTokenExpired_validToken_returnsFalse() {
        String token = jwtTokenProvider.generateToken(testUser);

        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }
}
