package hu.kornel.server.application.usecase.difficulty;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.DifficultyLevel;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.repository.ExerciseAttemptRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateUserDifficultyUseCase {
    
    private final UserRepositoryInterface userRepository;
    private final ExerciseAttemptRepositoryInterface exerciseAttemptRepository;
    
    @Transactional
    public void execute(Long userId, DifficultyLevel newLevel) {
        log.info("Updating difficulty level for user {} to {}", userId, newLevel);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        DifficultyLevel oldLevel = user.getDifficultyLevelOrDefault();
        
        user.updateDifficultyLevel(newLevel);
        userRepository.save(user);
        
        log.info("Difficulty level updated successfully for user {}", userId);
        
        
        
        if (oldLevel != newLevel) {
            log.info("Difficulty changed from {} to {}, cleaning up all attempts for user {}", 
                     oldLevel, newLevel, userId);
            cleanupOldAttempts(userId);
        }
    }
    
    
    private void cleanupOldAttempts(Long userId) {
        try {
            exerciseAttemptRepository.deleteByUserId(userId);
            log.info("Successfully cleaned up all attempts for user {}", userId);
        } catch (Exception e) {
            log.error("Error cleaning up attempts for user {}: {}", userId, e.getMessage());
            
        }
    }
}