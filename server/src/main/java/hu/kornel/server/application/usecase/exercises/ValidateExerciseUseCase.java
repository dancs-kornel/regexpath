package hu.kornel.server.application.usecase.exercises;

import java.util.Optional;

import org.springframework.stereotype.Service;

import hu.kornel.server.application.dto.ExerciseDto;
import hu.kornel.server.application.dto.validation.ValidationRequestDto;
import hu.kornel.server.application.dto.validation.ValidationResponseDto;
import hu.kornel.server.application.service.ExerciseValidationService;
import hu.kornel.server.domain.repository.LessonRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidateExerciseUseCase {

    private final LessonRepositoryInterface lessonRepository;
    private final ExerciseValidationService validationService;

    public Optional<ValidationResponseDto> execute(String lessonId, String exerciseId, ValidationRequestDto request) {
        log.debug("Validating exercise: lessonId={} exerciseId={}", lessonId, exerciseId);

        return lessonRepository.getExerciseById(lessonId, exerciseId)
                .map(exercise -> {
                    log.debug("Found exercise: {} of type {}", exercise.getTitle(), exercise.getType());
                    return validationService.validateExercise(exercise, request);
                });
    }
}
