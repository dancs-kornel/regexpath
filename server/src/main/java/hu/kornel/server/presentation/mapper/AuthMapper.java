package hu.kornel.server.presentation.mapper;

import org.springframework.stereotype.Component;

import hu.kornel.server.application.dto.AuthenticationResponseDto;
import hu.kornel.server.application.dto.LoginCommand;
import hu.kornel.server.application.dto.RegisterUserCommand;
import hu.kornel.server.application.dto.UserDto;
import hu.kornel.server.application.dto.authentication.AuthResponseDto;
import hu.kornel.server.application.dto.authentication.LoginRequestDto;
import hu.kornel.server.application.dto.authentication.RegisterRequestDto;
import hu.kornel.server.application.dto.authentication.UserResponseDto;

@Component
public class AuthMapper {
    public RegisterUserCommand toCommand(RegisterRequestDto request) {
        return RegisterUserCommand.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(request.getPassword())
        .role(request.getRole())
        .build();
    }

    public LoginCommand toCommand(LoginRequestDto request) {
        return LoginCommand.builder()
        .emailOrUsername(request.getEmailOrUsername())
        .password(request.getPassword())
        .build();
    }

    public AuthResponseDto toResponse(AuthenticationResponseDto dto) {
        return AuthResponseDto.builder()
                .token(dto.getToken())
                .user(toUserResponse(dto.getUser()))
                .build();
    }

    public UserResponseDto toUserResponse(UserDto dto) {
        return UserResponseDto.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .role(dto.getRole())
                .createdAt(dto.getCreatedAt())
                .lastLogin(dto.getLastLogin())
                .active(dto.isActive())
                .build();
    }
}
