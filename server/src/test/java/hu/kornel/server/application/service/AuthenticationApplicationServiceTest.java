package hu.kornel.server.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.kornel.server.application.dto.AuthenticationResponseDto;
import hu.kornel.server.application.dto.LoginCommand;
import hu.kornel.server.application.dto.RegisterUserCommand;
import hu.kornel.server.application.dto.UserDto;
import hu.kornel.server.application.usecase.GetCurrentUserUseCase;
import hu.kornel.server.application.usecase.LoginUserUseCase;
import hu.kornel.server.application.usecase.RegisterUserUseCase;
import hu.kornel.server.domain.entities.UserRole;

@ExtendWith(MockitoExtension.class)
class AuthenticationApplicationServiceTest {

    @Mock
    RegisterUserUseCase registerUserUseCase;

    @Mock
    LoginUserUseCase loginUserUseCase;

    @Mock
    GetCurrentUserUseCase getCurrentUserUseCase;

    @InjectMocks
    AuthenticationApplicationService service;

    @Test
    @DisplayName("register: delegates to RegisterUserUseCase and returns response")
    void register_success() {
        var command = mock(RegisterUserCommand.class);
        var expectedResponse = AuthenticationResponseDto.builder()
                .token("jwt-token")
                .user(UserDto.builder()
                        .id(1L)
                        .username("newuser")
                        .email("newuser@example.com")
                        .role(UserRole.STUDENT)
                        .active(true)
                        .build())
                .build();

        when(registerUserUseCase.execute(command)).thenReturn(expectedResponse);

        AuthenticationResponseDto result = service.register(command);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUser().getUsername()).isEqualTo("newuser");
        verify(registerUserUseCase).execute(command);
    }

    @Test
    @DisplayName("login: delegates to LoginUserUseCase and returns response")
    void login_success() {
        var command = mock(LoginCommand.class);
        var expectedResponse = AuthenticationResponseDto.builder()
                .token("login-token")
                .user(UserDto.builder()
                        .id(2L)
                        .username("existinguser")
                        .email("existing@example.com")
                        .role(UserRole.TEACHER)
                        .active(true)
                        .build())
                .build();

        when(loginUserUseCase.execute(command)).thenReturn(expectedResponse);

        AuthenticationResponseDto result = service.login(command);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.getToken()).isEqualTo("login-token");
        assertThat(result.getUser().getRole()).isEqualTo(UserRole.TEACHER);
        verify(loginUserUseCase).execute(command);
    }

    @Test
    @DisplayName("getCurrentUser: delegates to GetCurrentUserUseCase with token")
    void getCurrentUser_success() {
        String token = "Bearer jwt-token";
        var expectedUser = UserDto.builder()
                .id(3L)
                .username("currentuser")
                .email("current@example.com")
                .role(UserRole.STUDENT)
                .active(true)
                .build();

        when(getCurrentUserUseCase.execute(token)).thenReturn(expectedUser);

        UserDto result = service.getCurrentUser(token);

        assertThat(result).isEqualTo(expectedUser);
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getUsername()).isEqualTo("currentuser");
        verify(getCurrentUserUseCase).execute(token);
    }

    @Test
    @DisplayName("getCurrentUser: returns null when token is invalid")
    void getCurrentUser_invalidToken() {
        String invalidToken = "invalid-token";

        when(getCurrentUserUseCase.execute(invalidToken)).thenReturn(null);

        UserDto result = service.getCurrentUser(invalidToken);

        assertThat(result).isNull();
        verify(getCurrentUserUseCase).execute(invalidToken);
    }
}
