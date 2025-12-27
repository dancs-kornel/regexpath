package hu.kornel.server.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.AuthenticationResponseDto;
import hu.kornel.server.application.dto.LoginCommand;
import hu.kornel.server.application.dto.UserDto;
import hu.kornel.server.application.mapper.UserMapper;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.InvalidCredentialsException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.service.AuthenticationDomainServiceInterface;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginUserUseCase {
    private final UserRepositoryInterface userRepository;
    private final AuthenticationDomainServiceInterface authenticationService;

    @Transactional
    public AuthenticationResponseDto execute(LoginCommand command) {
        Optional<User> userOptional = userRepository.findByEmail(command.getEmailOrUsername());
        if (userOptional.isEmpty()) userOptional = userRepository.findByUsername(command.getEmailOrUsername());
        if (userOptional.isEmpty()) throw new InvalidCredentialsException();

        User user = userOptional.get();
        boolean passwordMatches = authenticationService.verifyPassword(command.getPassword(), user.getPasswordHash());
        if (!passwordMatches) throw new InvalidCredentialsException();
        if (!user.isActive()) throw new InvalidCredentialsException("User account is deactivated");
        
        user.updateLastLogin();
        userRepository.save(user);

        String token = authenticationService.generateToken(user);
        UserDto userDto = UserMapper.convertToDto(user);

        return AuthenticationResponseDto.builder()
        .token(token)
        .user(userDto)
        .build();
    }
}
