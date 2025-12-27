package hu.kornel.server.application.usecase.assignments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.kornel.server.application.dto.ExerciseDto;
import hu.kornel.server.application.dto.assignments.ExerciseAnswerRequest;
import hu.kornel.server.application.dto.validation.ChoiceValidationRequestDto;
import hu.kornel.server.application.dto.validation.RegexValidationRequestDto;
import hu.kornel.server.application.dto.validation.ValidationRequestDto;
import hu.kornel.server.application.dto.validation.ValidationResponseDto;
import hu.kornel.server.application.dto.validation.XPathValidationRequestDto;
import hu.kornel.server.application.service.ExerciseDtoConverter;
import hu.kornel.server.application.service.ExerciseValidationService;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.entities.assignments.ExerciseAnswer;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseAnswerRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmitAssignmentAttemptUseCase {

    private final AssignmentAttemptRepositoryInterface attemptRepository;
    private final ExerciseRepositoryInterface exerciseRepository;
    private final ExerciseAnswerRepositoryInterface answerRepository;
    private final ExerciseValidationService validationService;
    private final ExerciseDtoConverter exerciseDtoConverter;
    private final ObjectMapper objectMapper;

    @Transactional
    public AssignmentAttempt execute(Long attemptId, List<ExerciseAnswerRequest> answerRequests, Long userId) {
        log.debug("Submitting attempt {} by user {} with {} answers", attemptId, userId, answerRequests.size());

        AssignmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalStateException("Próbálkozás nem található"));

        validateAttemptSubmission(attempt, userId);

        List<Exercise> exercises = loadAssignmentExercises(attempt.getAssignmentId());
        Map<Long, Exercise> exerciseMap = exercises.stream()
                .collect(Collectors.toMap(Exercise::getId, e -> e));

        List<ExerciseAnswer> answers = gradeAnswers(attemptId, answerRequests, exerciseMap);
        int totalScore = answers.stream().mapToInt(ExerciseAnswer::getPointsEarned).sum();

        saveAnswersAndSubmit(attempt, answers, totalScore);

        log.info("Attempt {} submitted with score {}/{}", attemptId, totalScore, attempt.getMaxScore());
        return attempt;
    }

    private void validateAttemptSubmission(AssignmentAttempt attempt, Long userId) {
        if (!attempt.getStudentId().equals(userId)) {
            throw new IllegalStateException("Csak a saját próbálkozásaidat küldheted be");
        }

        if (attempt.isCompleted()) {
            throw new IllegalStateException("Ez a próbálkozás már be lett küldve");
        }

        if (attempt.isExpired()) {
            throw new IllegalStateException("A próbálkozás ideje lejárt");
        }
    }

    private List<Exercise> loadAssignmentExercises(Long assignmentId) {
        List<Exercise> exercises = exerciseRepository.findByAssignmentIdOrderByOrderIndex(assignmentId);

        if (exercises.isEmpty()) {
            throw new IllegalStateException("A feladathoz nem tartoznak gyakorlatok");
        }

        return exercises;
    }

    private List<ExerciseAnswer> gradeAnswers(Long attemptId, List<ExerciseAnswerRequest> answerRequests,
                                               Map<Long, Exercise> exerciseMap) {
        List<ExerciseAnswer> answers = new ArrayList<>();

        for (ExerciseAnswerRequest answerReq : answerRequests) {
            Exercise exercise = exerciseMap.get(answerReq.getExerciseId());
            if (exercise == null) {
                log.warn("Exercise {} not found in assignment", answerReq.getExerciseId());
                continue;
            }

            ExerciseAnswer answer = gradeExerciseAnswer(attemptId, answerReq, exercise);
            answers.add(answer);

            log.debug("Exercise {} validated: correct={}, points={}/{}",
                    exercise.getId(), answer.isCorrect(), answer.getPointsEarned(), answer.getPointsPossible());
        }

        return answers;
    }

    private ExerciseAnswer gradeExerciseAnswer(Long attemptId, ExerciseAnswerRequest answerReq, Exercise exercise) {
        ExerciseDto exerciseDto = exerciseDtoConverter.toDto(exercise);
        ValidationRequestDto validationRequest = createValidationRequest(exercise.getType(), answerReq.getAnswerJson());
        ValidationResponseDto validationResponse = validationService.validateExercise(exerciseDto, validationRequest);

        if (exercise.getPoints() == null) {
            throw new IllegalStateException("A gyakorlathoz nem tartozik pontszám");
        }

        int pointsEarned = validationResponse.isCorrect() ? exercise.getPoints() : 0;

        return ExerciseAnswer.builder()
                .attemptId(attemptId)
                .exerciseId(exercise.getId())
                .exerciseType(exercise.getType())
                .answerJson(answerReq.getAnswerJson())
                .correct(validationResponse.isCorrect())
                .pointsEarned(pointsEarned)
                .pointsPossible(exercise.getPoints())
                .validationResultJson(serializeValidationResult(validationResponse))
                .build();
    }

    private void saveAnswersAndSubmit(AssignmentAttempt attempt, List<ExerciseAnswer> answers, int totalScore) {
        answers.forEach(answerRepository::save);
        attempt.submit(totalScore);
        attemptRepository.save(attempt);
    }

    private ValidationRequestDto createValidationRequest(
            hu.kornel.server.domain.entities.assignments.ExerciseType type,
            String answerJson) {
        try {
            var answerNode = objectMapper.readTree(answerJson);

            return switch (type) {
                case MULTIPLE_CHOICE, RADIO -> {
                    ChoiceValidationRequestDto req = new ChoiceValidationRequestDto();
                    req.setSelectedOptions(objectMapper.convertValue(
                            answerNode.get("selectedOptions"),
                            objectMapper.getTypeFactory().constructCollectionType(
                                    List.class, String.class)));
                    yield req;
                }
                case REGEX_SANDBOX -> {
                    RegexValidationRequestDto req = new RegexValidationRequestDto();
                    req.setPattern(answerNode.get("pattern").asText());
                    yield req;
                }
                case XPATH_SANDBOX -> {
                    XPathValidationRequestDto req = new XPathValidationRequestDto();
                    req.setExpression(answerNode.get("expression").asText());
                    yield req;
                }
            };
        } catch (Exception e) {
            log.error("Failed to parse answer JSON", e);
            throw new IllegalArgumentException("Érvénytelen válasz formátum");
        }
    }

    private String serializeValidationResult(ValidationResponseDto response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to serialize validation result", e);
            return "{}";
        }
    }
}