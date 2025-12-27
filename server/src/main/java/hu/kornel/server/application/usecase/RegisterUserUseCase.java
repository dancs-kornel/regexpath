package hu.kornel.server.application.usecase;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.AuthenticationResponseDto;
import hu.kornel.server.application.dto.RegisterUserCommand;
import hu.kornel.server.application.dto.UserDto;
import hu.kornel.server.application.mapper.UserMapper;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.UserAlreadyExistsException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.service.AuthenticationDomainServiceInterface;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RegisterUserUseCase {
    private final UserRepositoryInterface userRepository;
    private final AuthenticationDomainServiceInterface authenticationService;

    @Transactional
    public AuthenticationResponseDto execute(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.getEmail())) throw UserAlreadyExistsException.withEmail(command.getEmail());
        if (userRepository.existsByUsername(command.getUsername())) throw UserAlreadyExistsException.withUsername(command.getUsername());

        String hashedPassword = authenticationService.hashPassword(command.getPassword());

        User user = User.builder().username(command.getUsername())
        .email(command.getEmail())
        .passwordHash(hashedPassword)
        .role(command.getRole())
        .createdAt(LocalDateTime.now())
        .active(true)
        .build();

        user.updateLastLogin();
        User savedUser = userRepository.save(user);

        String token = authenticationService.generateToken(savedUser);
        UserDto userDto = UserMapper.convertToDto(savedUser);
        return AuthenticationResponseDto.builder().token(token).user(userDto).build();
    }
}
