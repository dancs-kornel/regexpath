package hu.kornel.server.application.usecase.exercises;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import hu.kornel.server.application.dto.ExerciseDto;
import hu.kornel.server.application.dto.MultipleChoiceExerciseDto;
import hu.kornel.server.application.dto.RadioExerciseDto;
import hu.kornel.server.application.dto.RegexSandboxExerciseDto;
import hu.kornel.server.application.dto.SolutionResponseDto;
import hu.kornel.server.application.dto.XPathSandboxExerciseDto;
import hu.kornel.server.domain.repository.ExerciseAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.LessonRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetExerciseSolutionUseCase {

    private final LessonRepositoryInterface lessonRepository;
    private final ExerciseAttemptRepositoryInterface exerciseAttemptRepository;

    private static final int REQUIRED_ATTEMPTS_FOR_SOLUTION = 3;

    public Optional<SolutionResponseDto> execute(String lessonId, String exerciseId, Long userId) {
        log.debug("User {} requesting solution for exercise: lessonId={} exerciseId={}", userId, lessonId, exerciseId);

        long attemptCount = exerciseAttemptRepository.countAttemptsByUserIdAndExerciseId(
                userId, lessonId, exerciseId);

        log.debug("User {} has {} attempts for exercise {}", userId, attemptCount, exerciseId);

        if (attemptCount < REQUIRED_ATTEMPTS_FOR_SOLUTION) {
            log.warn("User {} has only {} attempts, need {} to view solution",
                    userId, attemptCount, REQUIRED_ATTEMPTS_FOR_SOLUTION);
            return Optional.empty();
        }

        return lessonRepository.getExerciseById(lessonId, exerciseId)
                .flatMap(exercise -> {
                    String solution = extractSolution(exercise);

                    if (solution == null) {
                        log.warn("No solution available for exercise type: {}", exercise.getType());
                        return Optional.empty();
                    }

                    log.info("User {} accessed solution for exercise {} after {} attempts",
                            userId, exerciseId, attemptCount);

                    return Optional.of(new SolutionResponseDto(solution, exercise.getType()));
                });
    }

    public long getAttemptCount(String lessonId, String exerciseId, Long userId) {
        return exerciseAttemptRepository.countAttemptsByUserIdAndExerciseId(userId, lessonId, exerciseId);
    }

    public int getRequiredAttempts() {
        return REQUIRED_ATTEMPTS_FOR_SOLUTION;
    }

    private String extractSolution(ExerciseDto exercise) {
        if (exercise instanceof RegexSandboxExerciseDto) {
            return ((RegexSandboxExerciseDto) exercise).getSolution();
        } else if (exercise instanceof XPathSandboxExerciseDto) {
            return ((XPathSandboxExerciseDto) exercise).getSolution();
        } else if (exercise instanceof MultipleChoiceExerciseDto) {
            return ((MultipleChoiceExerciseDto) exercise).getOptions().stream()
                    .filter(opt -> opt.isCorrect())
                    .map(opt -> opt.getId())
                    .collect(Collectors.joining(",", "[", "]"));
        } else if (exercise instanceof RadioExerciseDto) {
            return ((RadioExerciseDto) exercise).getOptions().stream()
                    .filter(opt -> opt.isCorrect())
                    .map(opt -> opt.getId())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
