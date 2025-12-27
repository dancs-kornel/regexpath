package hu.kornel.server.application.usecase.difficulty;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecordDifficultyPromptShownUseCase {
    
    private final UserRepositoryInterface userRepository;
    
    @Transactional
    public void execute(Long userId) {
        log.debug("Recording difficulty prompt shown for user {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        user.recordDifficultyPromptShown();
        userRepository.save(user);
        
        log.debug("Difficulty prompt timestamp updated for user {}", userId);
    }
}