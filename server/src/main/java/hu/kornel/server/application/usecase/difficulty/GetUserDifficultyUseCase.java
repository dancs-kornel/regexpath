package hu.kornel.server.application.usecase.difficulty;

import org.springframework.stereotype.Service;

import hu.kornel.server.domain.entities.DifficultyLevel;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetUserDifficultyUseCase {

    private final UserRepositoryInterface userRepository;

    public DifficultyLevel execute(Long userId) {
        log.debug("Getting difficulty level for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return user.getDifficultyLevelOrDefault();
    }
}
