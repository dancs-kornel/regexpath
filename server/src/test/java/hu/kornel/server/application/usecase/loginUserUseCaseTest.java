package hu.kornel.server.application.usecase;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.kornel.server.application.dto.AuthenticationResponseDto;
import hu.kornel.server.application.dto.LoginCommand;
import hu.kornel.server.application.dto.UserDto;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.UserRole;
import hu.kornel.server.domain.exception.InvalidCredentialsException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.service.AuthenticationDomainServiceInterface;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest { 

    @Mock
    UserRepositoryInterface userRepository;
    @Mock
    AuthenticationDomainServiceInterface authenticationService;
    @InjectMocks
    LoginUserUseCase useCase;

    @Test
    @DisplayName("email: verify password, update lastLogin, save, return token + user DTO")
    void loginByEmail_success() {
        var user = User.builder()
                .id(1L)
                .username("user")
                .email("user@example.com")
                .passwordHash("hash")
                .role(UserRole.STUDENT)
                .createdAt(LocalDateTime.now().minusDays(1))
                .active(true)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(authenticationService.verifyPassword("pw", "hash")).thenReturn(true);
        when(authenticationService.generateToken(user)).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var cmd = mock(LoginCommand.class);
        when(cmd.getEmailOrUsername()).thenReturn("user@example.com");
        when(cmd.getPassword()).thenReturn("pw");

        AuthenticationResponseDto response = useCase.execute(cmd);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser())
                .extracting(UserDto::getId, UserDto::getUsername, UserDto::getEmail, UserDto::getRole,
                        UserDto::isActive)
                .containsExactly(1L, "user", "user@example.com", UserRole.STUDENT, true);

        assertThat(user.getLastLogin()).isNotNull();
        verify(userRepository).save(user);
        verify(authenticationService).verifyPassword("pw", "hash");
        verify(authenticationService).generateToken(user);
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    @DisplayName("username: falls back to username lookup when email not found")
    void loginByUsername_success_afterBugfix() {
        var user = User.builder()
                .id(2L)
                .username("user")
                .email("user@example.com")
                .passwordHash("hash")
                .role(UserRole.STUDENT)
                .active(true)
                .build();

        when(userRepository.findByEmail("user")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(authenticationService.verifyPassword("pw", "hash")).thenReturn(true);
        when(authenticationService.generateToken(user)).thenReturn("jwt-token-2");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var cmd = mock(LoginCommand.class);
        when(cmd.getEmailOrUsername()).thenReturn("user");
        when(cmd.getPassword()).thenReturn("pw");

        AuthenticationResponseDto response = useCase.execute(cmd);

        assertThat(response.getToken()).isEqualTo("jwt-token-2");
        assertThat(user.getLastLogin()).isNotNull();
        verify(userRepository).findByEmail("user");
        verify(userRepository).findByUsername("user");
        verify(userRepository).save(user);
        verify(authenticationService).verifyPassword("pw", "hash");
        verify(authenticationService).generateToken(user);
    }

    @Test
    @DisplayName("Unknown user (email and username not found) -> InvalidCredentialsException")
    void userNotFound_throws() {
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        var cmd = mock(LoginCommand.class);
        when(cmd.getEmailOrUsername()).thenReturn("unknown");

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository).findByEmail("unknown");
        verify(userRepository).findByUsername("unknown");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("Bad password -> InvalidCredentialsException")
    void badPassword_throws() {
        var user = User.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .active(true)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(authenticationService.verifyPassword("bad", "hash")).thenReturn(false);

        var cmd = mock(LoginCommand.class);
        when(cmd.getEmailOrUsername()).thenReturn("user@example.com");
        when(cmd.getPassword()).thenReturn("bad");

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(authenticationService).verifyPassword("bad", "hash");
        verify(authenticationService, never()).generateToken(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Inactive user -> InvalidCredentialsException with message")
    void inactiveUser_throws() {
        var user = User.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .active(false)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(authenticationService.verifyPassword("pw", "hash")).thenReturn(true);

        var cmd = mock(LoginCommand.class);
        when(cmd.getEmailOrUsername()).thenReturn("user@example.com");
        when(cmd.getPassword()).thenReturn("pw");

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("deactivated");

        verify(authenticationService, never()).generateToken(any());
        verify(userRepository, never()).save(any());
    }
}
