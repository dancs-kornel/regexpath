package hu.kornel.server.application.usecase.difficulty;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.difficulty.DifficultyPromptResponse;
import hu.kornel.server.application.service.DifficultyCalculationService;
import hu.kornel.server.domain.entities.DifficultyLevel;
import hu.kornel.server.domain.entities.ExerciseAttempt;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.ExerciseAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.valueObjects.DifficultyStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecordExerciseAttemptUseCase {
    
    private final ExerciseAttemptRepositoryInterface exerciseAttemptRepository;
    private final UserRepositoryInterface userRepository;
    private final DifficultyCalculationService difficultyCalculationService;
    
    
    private static final long MIN_PROMPT_INTERVAL_MINUTES = 5;
    
    @Transactional
    public DifficultyPromptResponse execute(Long userId, String lessonId, String exerciseId, 
                                           boolean isCorrect, int attemptNumber) {
        log.debug("Recording exercise attempt: userId={}, lessonId={}, exerciseId={}, correct={}, attemptNumber={}",
                userId, lessonId, exerciseId, isCorrect, attemptNumber);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        
        ExerciseAttempt attempt = ExerciseAttempt.builder()
                .userId(userId)
                .lessonId(lessonId)
                .exerciseId(exerciseId)
                .attemptNumber(attemptNumber)
                .correct(isCorrect)
                .attemptedAt(LocalDateTime.now())
                .build();
        
        exerciseAttemptRepository.save(attempt);
        log.debug("Attempt recorded successfully");
        
        
        
        if (!isCorrect) {
            log.debug("Answer was incorrect, not checking for difficulty prompt");
            return buildNoPromptResponse();
        }
        
        
        if (wasPromptShownRecently(user)) {
            log.debug("Prompt was shown recently, skipping");
            return buildNoPromptResponse();
        }
        
        
        DifficultyStats stats = difficultyCalculationService.calculateStats(
                userId, user.getDifficultyLevelOrDefault());
        
        
        if (stats.shouldPromptIncrease()) {
            log.info("User {} should be prompted to INCREASE difficulty", userId);
            return buildPromptResponse(stats, "INCREASE");
        } else if (stats.shouldPromptDecrease()) {
            log.info("User {} should be prompted to DECREASE difficulty", userId);
            return buildPromptResponse(stats, "DECREASE");
        }
        
        log.debug("No difficulty prompt needed");
        return buildNoPromptResponse();
    }
    
    private boolean wasPromptShownRecently(User user) {
        if (user.getDifficultyPromptShownAt() == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastPrompt = user.getDifficultyPromptShownAt();
        long minutesSinceLastPrompt = java.time.Duration.between(lastPrompt, now).toMinutes();
        
        return minutesSinceLastPrompt < MIN_PROMPT_INTERVAL_MINUTES;
    }
    
    private DifficultyPromptResponse buildNoPromptResponse() {
        return DifficultyPromptResponse.builder()
                .shouldPrompt(false)
                .build();
    }
    
    private DifficultyPromptResponse buildPromptResponse(DifficultyStats stats, String promptType) {
        DifficultyLevel suggestedLevel = promptType.equals("INCREASE") 
                ? stats.getCurrentLevel().increase()
                : stats.getCurrentLevel().decrease();
        
        String message = promptType.equals("INCREASE")
                ? "Észrevettük, hogy ügyesen teljesítesz! Szeretnéd nehezíteni a feladatokat?"
                : "Észrevettük, hogy ezek a feladatok kihívást jelentenek. Szeretnéd könnyíteni őket?";
        
        return DifficultyPromptResponse.builder()
                .shouldPrompt(true)
                .promptType(promptType)
                .currentLevel(stats.getCurrentLevel())
                .suggestedLevel(suggestedLevel)
                .message(message)
                .consecutiveCorrectFirstAttempts(stats.getConsecutiveCorrectFirstAttempts())
                .consecutiveStrugglingExercises(stats.getConsecutiveStrugglingExercises())
                .build();
    }
}