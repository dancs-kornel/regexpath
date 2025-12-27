package hu.kornel.server.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.UserDto;
import hu.kornel.server.application.mapper.UserMapper;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.InvalidTokenException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.service.AuthenticationDomainServiceInterface;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetCurrentUserUseCase {
    private final UserRepositoryInterface userRepository;
    private final AuthenticationDomainServiceInterface authenticationService;

    @Transactional(readOnly = true)
    public UserDto execute(String token) {
        Long userId = authenticationService.validateTokenAndGetUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> UserNotFoundException.withId(userId));

        if (!user.isActive()) throw new InvalidTokenException("User account is deactivated");

        return UserMapper.convertToDto(user);
    }
}
