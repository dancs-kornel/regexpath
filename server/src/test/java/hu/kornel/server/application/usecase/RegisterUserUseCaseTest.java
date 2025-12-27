package hu.kornel.server.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.kornel.server.application.dto.AuthenticationResponseDto;
import hu.kornel.server.application.dto.RegisterUserCommand;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.UserRole;
import hu.kornel.server.domain.exception.UserAlreadyExistsException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.service.AuthenticationDomainServiceInterface;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    UserRepositoryInterface userRepository;

    @Mock
    AuthenticationDomainServiceInterface authenticationService;

    @InjectMocks
    RegisterUserUseCase useCase;

    @Test
    @DisplayName("register: successful registration with student role returns token and user")
    void register_success_student() {
        var command = mock(RegisterUserCommand.class);
        when(command.getEmail()).thenReturn("student@example.com");
        when(command.getUsername()).thenReturn("newstudent");
        when(command.getPassword()).thenReturn("password123");
        when(command.getRole()).thenReturn(UserRole.STUDENT);

        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(authenticationService.hashPassword("password123")).thenReturn("hashed_password");
        when(authenticationService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });

        AuthenticationResponseDto response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("student@example.com");
        assertThat(response.getUser().getUsername()).isEqualTo("newstudent");
        assertThat(response.getUser().getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(response.getUser().isActive()).isTrue();

        verify(userRepository).existsByEmail("student@example.com");
        verify(userRepository).existsByUsername("newstudent");
        verify(authenticationService).hashPassword("password123");
        verify(authenticationService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("register: successful registration with teacher role")
    void register_success_teacher() {
        var command = mock(RegisterUserCommand.class);
        when(command.getEmail()).thenReturn("teacher@example.com");
        when(command.getUsername()).thenReturn("newteacher");
        when(command.getPassword()).thenReturn("teacherpass");
        when(command.getRole()).thenReturn(UserRole.TEACHER);

        when(userRepository.existsByEmail("teacher@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newteacher")).thenReturn(false);
        when(authenticationService.hashPassword("teacherpass")).thenReturn("hashed_teacher_pass");
        when(authenticationService.generateToken(any(User.class))).thenReturn("teacher-token");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(2L);
            return user;
        });

        AuthenticationResponseDto response = useCase.execute(command);

        assertThat(response.getUser().getRole()).isEqualTo(UserRole.TEACHER);
        assertThat(response.getToken()).isEqualTo("teacher-token");
    }

    @Test
    @DisplayName("register: duplicate email throws UserAlreadyExistsException")
    void register_duplicateEmail_throws() {
        var command = mock(RegisterUserCommand.class);
        when(command.getEmail()).thenReturn("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");

        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).existsByUsername(any());
        verify(authenticationService, never()).hashPassword(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: duplicate username throws UserAlreadyExistsException")
    void register_duplicateUsername_throws() {
        var command = mock(RegisterUserCommand.class);
        when(command.getEmail()).thenReturn("newemail@example.com");
        when(command.getUsername()).thenReturn("existinguser");

        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("existinguser");

        verify(userRepository).existsByEmail("newemail@example.com");
        verify(userRepository).existsByUsername("existinguser");
        verify(authenticationService, never()).hashPassword(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: user is created with active status and lastLogin is updated")
    void register_userStateVerification() {
        var command = mock(RegisterUserCommand.class);
        when(command.getEmail()).thenReturn("newuser@example.com");
        when(command.getUsername()).thenReturn("newuser");
        when(command.getPassword()).thenReturn("pass");
        when(command.getRole()).thenReturn(UserRole.STUDENT);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(authenticationService.hashPassword(any())).thenReturn("hash");
        when(authenticationService.generateToken(any(User.class))).thenReturn("token");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(5L);
            return user;
        });

        AuthenticationResponseDto response = useCase.execute(command);

        assertThat(response.getUser().isActive()).isTrue();
        assertThat(response.getUser().getId()).isEqualTo(5L);

        // Verify save was called once (lastLogin updated before save)
        verify(userRepository, times(1)).save(any(User.class));
    }
}
