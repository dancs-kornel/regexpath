package hu.kornel.server.presentation.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.kornel.server.application.dto.AuthenticationResponseDto;
import hu.kornel.server.application.dto.LoginCommand;
import hu.kornel.server.application.dto.RegisterUserCommand;
import hu.kornel.server.application.dto.UserDto;
import hu.kornel.server.application.dto.authentication.LoginRequestDto;
import hu.kornel.server.application.dto.authentication.RegisterRequestDto;
import hu.kornel.server.application.service.AuthenticationApplicationService;
import hu.kornel.server.domain.entities.UserRole;
import hu.kornel.server.domain.exception.InvalidCredentialsException;
import hu.kornel.server.domain.exception.InvalidTokenException;
import hu.kornel.server.domain.exception.UserAlreadyExistsException;
import hu.kornel.server.presentation.exception.GlobalExceptionHandler;
import hu.kornel.server.presentation.mapper.AuthMapper;

@WebMvcTest(controllers = AuthController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
    })
@ContextConfiguration(classes = {AuthController.class, AuthMapper.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationApplicationService authenticationService;

    // POST /api/auth/register 

    @Test
    @DisplayName("POST /api/auth/register: successful registration returns 201 with token and user")
    void register_success_returns201() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .role(UserRole.STUDENT)
                .build();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("newuser")
                .email("newuser@example.com")
                .role(UserRole.STUDENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();

        AuthenticationResponseDto responseDto = AuthenticationResponseDto.builder()
                .token("jwt-token-123")
                .user(userDto)
                .build();

        when(authenticationService.register(any(RegisterUserCommand.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", is("jwt-token-123")))
                .andExpect(jsonPath("$.user.id", is(1)))
                .andExpect(jsonPath("$.user.username", is("newuser")))
                .andExpect(jsonPath("$.user.email", is("newuser@example.com")))
                .andExpect(jsonPath("$.user.role", is("STUDENT")))
                .andExpect(jsonPath("$.user.active", is(true)))
                .andExpect(jsonPath("$.user.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register: duplicate email returns 409 Conflict")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newuser")
                .email("existing@example.com")
                .password("password123")
                .role(UserRole.STUDENT)
                .build();

        when(authenticationService.register(any(RegisterUserCommand.class)))
                .thenThrow(new UserAlreadyExistsException("User with email existing@example.com already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("User Already Exists")))
                .andExpect(jsonPath("$.message", is("User with email existing@example.com already exists")));
    }

    @Test
    @DisplayName("POST /api/auth/register: duplicate username returns 409 Conflict")
    void register_duplicateUsername_returns409() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("existinguser")
                .email("new@example.com")
                .password("password123")
                .role(UserRole.STUDENT)
                .build();

        when(authenticationService.register(any(RegisterUserCommand.class)))
                .thenThrow(new UserAlreadyExistsException("User with username existinguser already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("User Already Exists")))
                .andExpect(jsonPath("$.message", is("User with username existinguser already exists")));
    }

    @Test
    @DisplayName("POST /api/auth/register: invalid email format returns 400 Bad Request")
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newuser")
                .email("not-an-email")
                .password("password123")
                .role(UserRole.STUDENT)
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register: short password returns 400 Bad Request")
    void register_shortPassword_returns400() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("12345")
                .role(UserRole.STUDENT)
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register: missing required fields returns 400 Bad Request")
    void register_missingFields_returns400() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("newuser")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // POST /api/auth/login 

    @Test
    @DisplayName("POST /api/auth/login: successful login returns 200 with token and user")
    void login_success_returns200() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .emailOrUsername("testuser@example.com")
                .password("password123")
                .build();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@example.com")
                .role(UserRole.STUDENT)
                .active(true)
                .createdAt(LocalDateTime.now().minusDays(7))
                .lastLogin(LocalDateTime.now())
                .build();

        AuthenticationResponseDto responseDto = AuthenticationResponseDto.builder()
                .token("jwt-login-token")
                .user(userDto)
                .build();

        when(authenticationService.login(any(LoginCommand.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("jwt-login-token")))
                .andExpect(jsonPath("$.user.id", is(1)))
                .andExpect(jsonPath("$.user.username", is("testuser")))
                .andExpect(jsonPath("$.user.email", is("testuser@example.com")));
    }

    @Test
    @DisplayName("POST /api/auth/login: login by username succeeds")
    void login_byUsername_returns200() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .emailOrUsername("testuser")
                .password("password123")
                .build();

        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@example.com")
                .role(UserRole.STUDENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();

        AuthenticationResponseDto responseDto = AuthenticationResponseDto.builder()
                .token("jwt-login-token")
                .user(userDto)
                .build();

        when(authenticationService.login(any(LoginCommand.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("jwt-login-token")))
                .andExpect(jsonPath("$.user.username", is("testuser")));
    }

    @Test
    @DisplayName("POST /api/auth/login: invalid credentials returns 401 Unauthorized")
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .emailOrUsername("testuser@example.com")
                .password("wrongpassword")
                .build();

        when(authenticationService.login(any(LoginCommand.class)))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Authentication Failed")))
                .andExpect(jsonPath("$.message", is("Invalid credentials")));
    }

    @Test
    @DisplayName("POST /api/auth/login: missing password returns 400 Bad Request")
    void login_missingPassword_returns400() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .emailOrUsername("testuser@example.com")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login: inactive user returns 401 Unauthorized")
    void login_inactiveUser_returns401() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .emailOrUsername("inactive@example.com")
                .password("password123")
                .build();

        when(authenticationService.login(any(LoginCommand.class)))
                .thenThrow(new InvalidCredentialsException("User account has been deactivated"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Authentication Failed")))
                .andExpect(jsonPath("$.message", is("User account has been deactivated")));
    }

    @Test
    @DisplayName("GET /api/auth/me: valid token returns 200 with user details")
    void getCurrentUser_validToken_returns200() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@example.com")
                .role(UserRole.STUDENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();

        when(authenticationService.getCurrentUser("valid-token")).thenReturn(userDto);

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("testuser@example.com")))
                .andExpect(jsonPath("$.role", is("STUDENT")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("GET /api/auth/me: missing Authorization header returns 401 Unauthorized")
    void getCurrentUser_missingAuthHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/me: malformed Authorization header returns 401 Unauthorized")
    void getCurrentUser_malformedAuthHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/me: expired token returns 401 Unauthorized")
    void getCurrentUser_expiredToken_returns401() throws Exception {
        when(authenticationService.getCurrentUser("expired-token"))
                .thenThrow(new InvalidTokenException("Token has expired"));

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Invalid Token")))
                .andExpect(jsonPath("$.message", is("Token has expired")));
    }

    @Test
    @DisplayName("GET /api/auth/me: invalid token returns 401 Unauthorized")
    void getCurrentUser_invalidToken_returns401() throws Exception {
        when(authenticationService.getCurrentUser("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Invalid Token")))
                .andExpect(jsonPath("$.message", is("Invalid token")));
    }

    @Test
    @DisplayName("GET /api/auth/me: teacher role is correctly returned")
    void getCurrentUser_teacherRole_returns200() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(2L)
                .username("teacher")
                .email("teacher@example.com")
                .role(UserRole.TEACHER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();

        when(authenticationService.getCurrentUser("teacher-token")).thenReturn(userDto);

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer teacher-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("teacher")))
                .andExpect(jsonPath("$.role", is("TEACHER")));
    }
}
