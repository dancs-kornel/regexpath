package hu.kornel.server.presentation.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.kornel.server.application.dto.difficulty.DifficultyPromptResponse;
import hu.kornel.server.application.dto.difficulty.RecordExerciseAttemptRequest;
import hu.kornel.server.application.dto.difficulty.UpdateDifficultyRequest;
import hu.kornel.server.application.usecase.difficulty.GetUserDifficultyUseCase;
import hu.kornel.server.application.usecase.difficulty.RecordDifficultyPromptShownUseCase;
import hu.kornel.server.application.usecase.difficulty.RecordExerciseAttemptUseCase;
import hu.kornel.server.application.usecase.difficulty.UpdateUserDifficultyUseCase;
import hu.kornel.server.domain.entities.DifficultyLevel;
import hu.kornel.server.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/difficulty")
@RequiredArgsConstructor
@Slf4j
public class DifficultyController {

    private final GetUserDifficultyUseCase getUserDifficultyUseCase;
    private final RecordExerciseAttemptUseCase recordExerciseAttemptUseCase;
    private final UpdateUserDifficultyUseCase updateUserDifficultyUseCase;
    private final RecordDifficultyPromptShownUseCase recordDifficultyPromptShownUseCase;
    
    
    @GetMapping
    public ResponseEntity<Map<String, DifficultyLevel>> getDifficultyLevel(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.debug("GET /api/difficulty for user {}", userPrincipal.getId());

        DifficultyLevel difficultyLevel = getUserDifficultyUseCase.execute(userPrincipal.getId());

        return ResponseEntity.ok(Map.of("difficultyLevel", difficultyLevel));
    }
    
    
    @PostMapping("/record-attempt")
    public ResponseEntity<DifficultyPromptResponse> recordAttempt(
            @Valid @RequestBody RecordExerciseAttemptRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.debug("POST /api/difficulty/record-attempt for user {}", userPrincipal.getId());
        
        DifficultyPromptResponse response = recordExerciseAttemptUseCase.execute(
                userPrincipal.getId(),
                request.getLessonId(),
                request.getExerciseId(),
                request.getIsCorrect(),
                request.getAttemptNumber());
        
        return ResponseEntity.ok(response);
    }
    
    
    @PutMapping
    public ResponseEntity<Void> updateDifficulty(
            @Valid @RequestBody UpdateDifficultyRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("PUT /api/difficulty - User {} changing difficulty to {}", 
                userPrincipal.getId(), request.getDifficultyLevel());
        
        updateUserDifficultyUseCase.execute(userPrincipal.getId(), request.getDifficultyLevel());
        
        return ResponseEntity.ok().build();
    }
    
    
    @PostMapping("/prompt-shown")
    public ResponseEntity<Void> recordPromptShown(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.debug("POST /api/difficulty/prompt-shown for user {}", userPrincipal.getId());
        
        recordDifficultyPromptShownUseCase.execute(userPrincipal.getId());
        
        return ResponseEntity.ok().build();
    }
}