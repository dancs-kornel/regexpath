package hu.kornel.server.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import hu.kornel.server.domain.entities.DifficultyLevel;
import hu.kornel.server.domain.entities.ExerciseAttempt;
import hu.kornel.server.domain.repository.ExerciseAttemptRepositoryInterface;
import hu.kornel.server.domain.valueObjects.DifficultyStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DifficultyCalculationService {
    
    private final ExerciseAttemptRepositoryInterface exerciseAttemptRepository;
    
    private static final int RECENT_ATTEMPTS_LIMIT = 20; 
    private static final int STRUGGLING_THRESHOLD = 3; 
    
    public DifficultyStats calculateStats(Long userId, DifficultyLevel currentLevel) {
        log.debug("Calculating difficulty stats for user: {}", userId);
        
        List<ExerciseAttempt> recentAttempts = exerciseAttemptRepository
                .findRecentByUserId(userId, RECENT_ATTEMPTS_LIMIT);
        
        if (recentAttempts.isEmpty()) {
            log.debug("No recent attempts found for user: {}", userId);
            return DifficultyStats.builder()
                    .consecutiveCorrectFirstAttempts(0)
                    .consecutiveStrugglingExercises(0)
                    .currentExerciseAttempts(0)
                    .currentLevel(currentLevel)
                    .build();
        }
        
        int consecutiveCorrectFirstAttempts = calculateConsecutiveCorrectFirstAttempts(recentAttempts);
        int consecutiveStrugglingExercises = calculateConsecutiveStrugglingExercises(recentAttempts);
        
        log.debug("Stats for user {}: consecutive correct first attempts={}, consecutive struggling={}",
                userId, consecutiveCorrectFirstAttempts, consecutiveStrugglingExercises);
        
        return DifficultyStats.builder()
                .consecutiveCorrectFirstAttempts(consecutiveCorrectFirstAttempts)
                .consecutiveStrugglingExercises(consecutiveStrugglingExercises)
                .currentExerciseAttempts(0) 
                .currentLevel(currentLevel)
                .build();
    }

    private int calculateConsecutiveCorrectFirstAttempts(List<ExerciseAttempt> attempts) {
        int count = 0;
        String lastExerciseId = null;
        
        for (ExerciseAttempt attempt : attempts) {
            String exerciseKey = attempt.getLessonId() + ":" + attempt.getExerciseId();
            
            if (exerciseKey.equals(lastExerciseId)) { continue; }
            
            if (attempt.isCorrectFirstAttempt()) {
                count++;
                lastExerciseId = exerciseKey;
            } else {
                break;
            }
        }
        
        return count;
    }
    
    private int calculateConsecutiveStrugglingExercises(List<ExerciseAttempt> attempts) {
        int count = 0;
        String lastExerciseId = null;
        int currentExerciseAttempts = 0;
        
        for (ExerciseAttempt attempt : attempts) {
            String exerciseKey = attempt.getLessonId() + ":" + attempt.getExerciseId();
            
            if (!exerciseKey.equals(lastExerciseId)) {
                if (lastExerciseId != null) {
                    if (currentExerciseAttempts >= STRUGGLING_THRESHOLD) {
                        count++;
                    } else {
                        break;
                    }
                }
                lastExerciseId = exerciseKey;
                currentExerciseAttempts = 1;
            } else {
                currentExerciseAttempts++;
            }
        }
        
        if (lastExerciseId != null && currentExerciseAttempts >= STRUGGLING_THRESHOLD) {
            count++;
        }
        
        return count;
    }
}