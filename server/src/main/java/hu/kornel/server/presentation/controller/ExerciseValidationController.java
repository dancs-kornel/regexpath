package hu.kornel.server.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.kornel.server.application.dto.SolutionResponseDto;
import hu.kornel.server.application.dto.validation.ValidationRequestDto;
import hu.kornel.server.application.dto.validation.ValidationResponseDto;
import hu.kornel.server.application.usecase.exercises.GetExerciseSolutionUseCase;
import hu.kornel.server.application.usecase.exercises.ValidateExerciseUseCase;
import hu.kornel.server.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@Slf4j
public class ExerciseValidationController {

    private final ValidateExerciseUseCase validateExerciseUseCase;
    private final GetExerciseSolutionUseCase getExerciseSolutionUseCase;

    @PostMapping("/{lessonId}/{exerciseId}/validate")
    public ResponseEntity<ValidationResponseDto> validateExercise(
            @PathVariable String lessonId,
            @PathVariable String exerciseId,
            @Valid @RequestBody ValidationRequestDto request) {

        log.debug("POST /api/exercises/{}/{}/validate - Validating exercise", lessonId, exerciseId);

        return validateExerciseUseCase.execute(lessonId, exerciseId, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Exercise not found: lessonId={} exerciseId={}", lessonId, exerciseId);
                    return ResponseEntity.notFound().build();
                });
    }


    @GetMapping("/{lessonId}/{exerciseId}/solution")
    public ResponseEntity<?> getSolution(
            @PathVariable String lessonId,
            @PathVariable String exerciseId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.debug("GET /api/exercises/{}/{}/solution - Requesting solution", lessonId, exerciseId);

        if (userPrincipal == null) {
            log.warn("Unauthenticated user attempted to access solution");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be logged in to view solutions");
        }

        var solutionOptional = getExerciseSolutionUseCase.execute(lessonId, exerciseId, userPrincipal.getId());

        if (solutionOptional.isPresent()) {
            return ResponseEntity.ok(solutionOptional.get());
        }

        long attemptCount = getExerciseSolutionUseCase.getAttemptCount(lessonId, exerciseId, userPrincipal.getId());
        int requiredAttempts = getExerciseSolutionUseCase.getRequiredAttempts();

        if (attemptCount < requiredAttempts) {
            log.warn("User {} has only {} attempts, need {} to view solution",
                    userPrincipal.getId(), attemptCount, requiredAttempts);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format("You need at least %d attempts before viewing the solution. Current attempts: %d",
                            requiredAttempts, attemptCount));
        }

        log.warn("Exercise not found or no solution available: lessonId={} exerciseId={}", lessonId, exerciseId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Exercise not found or no solution available");
    }
}
