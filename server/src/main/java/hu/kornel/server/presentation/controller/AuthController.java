package hu.kornel.server.presentation.controller;

import static hu.kornel.server.infrastructure.util.AppConstants.BEARER_PREFIX;
import static hu.kornel.server.infrastructure.util.AppConstants.BEARER_PREFIX_LENGTH;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.kornel.server.application.dto.AuthenticationResponseDto;
import hu.kornel.server.application.dto.LoginCommand;
import hu.kornel.server.application.dto.RegisterUserCommand;
import hu.kornel.server.application.dto.UserDto;
import hu.kornel.server.application.dto.authentication.AuthResponseDto;
import hu.kornel.server.application.dto.authentication.LoginRequestDto;
import hu.kornel.server.application.dto.authentication.RegisterRequestDto;
import hu.kornel.server.application.dto.authentication.UserResponseDto;
import hu.kornel.server.application.service.AuthenticationApplicationService;
import hu.kornel.server.presentation.mapper.AuthMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationApplicationService authenticationService;
    private final AuthMapper authMapper;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        log.info("POST /api/auth/register - Registering user: {}", request.getUsername());
        RegisterUserCommand command = authMapper.toCommand(request);
        AuthenticationResponseDto response = authenticationService.register(command);

        log.info("User registered successfully: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(authMapper.toResponse(response));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("POST /api/auth/login - Login attempt of: {}", request.getEmailOrUsername());
        LoginCommand command = authMapper.toCommand(request);
        AuthenticationResponseDto response = authenticationService.login(command);

        log.info("User logged in successfully: {}", request.getEmailOrUsername());
        return ResponseEntity.ok(authMapper.toResponse(response));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@RequestHeader(value="Authorization",required=false)String authHeader) {
        log.debug("GET /api/auth/me - Getting current user");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("No valid Authorization header found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        UserDto user = authenticationService.getCurrentUser(token);

        log.debug("Current user retrieved: {}", user.getUsername());
        return ResponseEntity.ok(authMapper.toUserResponse(user));
    }
}
