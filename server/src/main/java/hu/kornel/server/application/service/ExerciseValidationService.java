package hu.kornel.server.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import hu.kornel.server.application.dto.ExerciseDto;
import hu.kornel.server.application.dto.MultipleChoiceExerciseDto;
import hu.kornel.server.application.dto.OptionDto;
import hu.kornel.server.application.dto.RadioExerciseDto;
import hu.kornel.server.application.dto.RegexSandboxExerciseDto;
import hu.kornel.server.application.dto.XPathSandboxExerciseDto;
import hu.kornel.server.application.dto.validation.ChoiceValidationRequestDto;
import hu.kornel.server.application.dto.validation.ChoiceValidationResponseDto;
import hu.kornel.server.application.dto.validation.PatternValidationResultDto;
import hu.kornel.server.application.dto.validation.RegexValidationRequestDto;
import hu.kornel.server.application.dto.validation.RegexValidationResponseDto;
import hu.kornel.server.application.dto.validation.TestCaseResultDto;
import hu.kornel.server.application.dto.validation.ValidationRequestDto;
import hu.kornel.server.application.dto.validation.ValidationResponseDto;
import hu.kornel.server.application.dto.validation.XPathValidationRequestDto;
import hu.kornel.server.application.dto.validation.XPathValidationResponseDto;
import hu.kornel.server.application.dto.validation.XPathValidationResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseValidationService {

	private final RegexValidationService regexValidationService;
	private final XPathValidationService xpathValidationService;

	public ValidationResponseDto validateExercise(ExerciseDto exercise, ValidationRequestDto request) {
		log.debug("Validating exercise: {} of type {}", exercise.getId(), exercise.getType());

		if (exercise instanceof MultipleChoiceExerciseDto multipleChoice
				&& request instanceof ChoiceValidationRequestDto choiceRequest) {
			return validateMultipleChoice(multipleChoice, choiceRequest);
		}

		if (exercise instanceof RadioExerciseDto radio
				&& request instanceof ChoiceValidationRequestDto choiceRequest) {
			return validateRadio(radio, choiceRequest);
		}

		if (exercise instanceof RegexSandboxExerciseDto regexSandbox
				&& request instanceof RegexValidationRequestDto regexRequest) {
			return validateRegexSandbox(regexSandbox, regexRequest);
		}

		if (exercise instanceof XPathSandboxExerciseDto xpathSandbox
				&& request instanceof XPathValidationRequestDto xpathRequest) {
			return validateXPathSandbox(xpathSandbox, xpathRequest);
		}

		return createUnsupportedTypeErrorResponse(exercise, request);
	}

	private ValidationResponseDto createUnsupportedTypeErrorResponse(ExerciseDto exercise,
			ValidationRequestDto request) {
		log.error("Unsupported exercise type or request mismatch: {} with {}",
				exercise.getClass().getSimpleName(), request.getClass().getSimpleName());

		String errorMessage = "Unsupported exercise type";

		if (request instanceof ChoiceValidationRequestDto) {
			ChoiceValidationResponseDto response = new ChoiceValidationResponseDto();
			response.setCorrect(false);
			response.setMessage(errorMessage);
			return response;
		}

		if (request instanceof XPathValidationRequestDto) {
			XPathValidationResponseDto response = new XPathValidationResponseDto();
			response.setCorrect(false);
			response.setMessage(errorMessage);
			return response;
		}

		RegexValidationResponseDto response = new RegexValidationResponseDto();
		response.setCorrect(false);
		response.setMessage(errorMessage);
		return response;
	}

	private XPathValidationResponseDto validateXPathSandbox(XPathSandboxExerciseDto exercise,
			XPathValidationRequestDto request) {
		log.debug("Validating XPath sandbox exercise: {}", exercise.getId());
		XPathValidationResponseDto response = new XPathValidationResponseDto();

		String validationError = validateExerciseConfiguration(exercise);
		if (validationError != null) {
			return createErrorResponse(response, validationError, request.getExpression());
		}

		XPathValidationResultDto userSyntaxResult = validateUserXPathSyntax(request.getExpression());
		if (userSyntaxResult.isInvalid()) {
			return createErrorResponse(response, userSyntaxResult.getErrorMessage(), request.getExpression());
		}

		XPathValidationResultDto solutionSyntaxResult = validateSolutionXPathSyntax(exercise);
		if (solutionSyntaxResult.isInvalid()) {
			return createErrorResponse(response,
					"A feladat konfigurációs hibát tartalmaz - érvénytelen megoldás XPath",
					request.getExpression());
		}

		return evaluateXPathExpressions(response, exercise, request.getExpression(),
				userSyntaxResult, solutionSyntaxResult);
	}

	private String validateExerciseConfiguration(XPathSandboxExerciseDto exercise) {
		if (exercise.getSampleDocument() == null || exercise.getSampleDocument().trim().isEmpty()) {
			log.error("Exercise {} has no sample document", exercise.getId());
			return "Hiányzik a mintadokumentum a feladatból";
		}

		if (exercise.getSolution() == null || exercise.getSolution().trim().isEmpty()) {
			log.error("Exercise {} has no solution XPath", exercise.getId());
			return "Hiányzik a megoldás a feladatból";
		}

		return null;
	}

	private XPathValidationResultDto validateUserXPathSyntax(String expression) {
		XPathValidationResultDto result = xpathValidationService.validateExpressionSyntax(expression);
		if (result.isInvalid()) {
			log.debug("User XPath validation failed: {}", result.getErrorMessage());
		} else {
			log.debug("User XPath syntax valid, proceeding with solution validation");
		}
		return result;
	}

	private XPathValidationResultDto validateSolutionXPathSyntax(XPathSandboxExerciseDto exercise) {
		XPathValidationResultDto result = xpathValidationService.validateExpressionSyntax(exercise.getSolution());
		if (result.isInvalid()) {
			log.error("Solution XPath is invalid for exercise {}: {}",
					exercise.getId(), result.getErrorMessage());
		}
		return result;
	}

	private XPathValidationResponseDto createErrorResponse(XPathValidationResponseDto response,
			String errorMessage, String expression) {
		populateXPathResponse(response, false, errorMessage, new ArrayList<>(), new ArrayList<>());
		response.setCompiledExpression(expression);
		return response;
	}

	private XPathValidationResponseDto evaluateXPathExpressions(XPathValidationResponseDto response,
			XPathSandboxExerciseDto exercise, String userExpression,
			XPathValidationResultDto userSyntaxResult, XPathValidationResultDto solutionSyntaxResult) {
		try {
			List<String> expectedNodes = xpathValidationService.evaluateXPath(
					solutionSyntaxResult.getCompiledExpression(), exercise.getSampleDocument());

			List<String> actualNodes = xpathValidationService.evaluateXPath(
					userSyntaxResult.getCompiledExpression(), exercise.getSampleDocument());

			log.debug("Expected nodes ({}): {}", expectedNodes.size(), expectedNodes);
			log.debug("Actual nodes ({}): {}", actualNodes.size(), actualNodes);

			boolean isCorrect = xpathValidationService.validateXPathMatch(actualNodes, expectedNodes);
			String explanationOrFeedback = isCorrect ? exercise.getExplanation()
					: generateFeedbackMessage(actualNodes, expectedNodes);

			populateXPathResponse(response, isCorrect, explanationOrFeedback, expectedNodes, actualNodes);
			response.setCompiledExpression(userExpression);

			return response;

		} catch (IllegalArgumentException e) {
			log.error("Document validation error for exercise {}: {}", exercise.getId(), e.getMessage());
			return createErrorResponse(response, e.getMessage(), userExpression);
		} catch (Exception e) {
			log.error("Error evaluating XPath expressions for exercise {}: {}",
					exercise.getId(), e.getMessage(), e);
			return createErrorResponse(response,
					"Hiba történt az XPath kifejezés kiértékelése során", userExpression);
		}
	}

	private String generateFeedbackMessage(List<String> actualNodes, List<String> expectedNodes) {
		if (actualNodes.isEmpty()) {
			return "Az XPath kifejezésed nem választott ki egyetlen elemet sem. " +
					"Várt elemek száma: " + expectedNodes.size();
		}

		if (actualNodes.size() < expectedNodes.size()) {
			return "Az XPath kifejezésed túl kevés elemet választott ki. " +
					"Kiválasztott: " + actualNodes.size() + ", várt: " + expectedNodes.size();
		}

		if (actualNodes.size() > expectedNodes.size()) {
			return "Az XPath kifejezésed túl sok elemet választott ki. " +
					"Kiválasztott: " + actualNodes.size() + ", várt: " + expectedNodes.size();
		}

		return "Az XPath kifejezésed helytelen elemeket választott ki. " +
				"Próbáld újra!";
	}

	private RegexValidationResponseDto validateRegexSandbox(RegexSandboxExerciseDto exercise,
			RegexValidationRequestDto request) {
		log.debug("Validating regex sandbox exercise: {}", exercise.getId());
		RegexValidationResponseDto response = new RegexValidationResponseDto();
		PatternValidationResultDto syntaxResult = regexValidationService.validatePatternSyntax(request.getPattern());
		if (syntaxResult.isInvalid()) {
			log.debug("Pattern validation failed: {}", syntaxResult.getErrorMessage());
			response.setCorrect(false);
			response.setMessage(syntaxResult.getErrorMessage());
			response.setExplanation("");
			response.setTestCaseResults(new ArrayList<>());
			response.setPassedTests(0);
			response.setTotalTests(0);
			response.setCompiledPattern(request.getPattern());
			return response;
		}
		log.debug("Pattern syntax valid, proceeding with test case validation");

		List<TestCaseResultDto> testResults = new ArrayList<>();
		if (exercise.getTestCases() != null && exercise.getTestCases().getPositive() != null) {
			List<TestCaseResultDto> positiveResults = regexValidationService.validatePositiveTestCases(
					syntaxResult.getCompiledPattern(), exercise.getTestCases().getPositive());
			testResults.addAll(positiveResults);
		}

		if (exercise.getTestCases() != null && exercise.getTestCases().getNegative() != null) {
			List<TestCaseResultDto> negativeResults = regexValidationService.validateNegativeTestCases(
					syntaxResult.getCompiledPattern(), exercise.getTestCases().getNegative());
			testResults.addAll(negativeResults);
		}

		long passedTests = testResults.stream().filter(TestCaseResultDto::isPassed).count();
		boolean allPassed = passedTests == testResults.size() && !testResults.isEmpty();
		populateRegexResponse(response, allPassed, exercise.getExplanation(), testResults);
		response.setCompiledPattern(request.getPattern());
		return response;
	}

	private ChoiceValidationResponseDto validateMultipleChoice(MultipleChoiceExerciseDto exercise,
			ChoiceValidationRequestDto request) {
		ChoiceValidationResponseDto response = new ChoiceValidationResponseDto();
		List<String> userAnswers = request.getSelectedOptions();
		log.debug("Validating multiple choice exercise with {} user answers", userAnswers.size());
		List<String> correctAnswers = exercise.getOptions().stream().filter(OptionDto::isCorrect).map(OptionDto::getId)
				.toList();
		log.debug("Expected {} correct answers: {}", correctAnswers.size(), correctAnswers);
		boolean isCorrect = userAnswers.size() == correctAnswers.size() && userAnswers.containsAll(correctAnswers);
		populateChoiceResponse(response, isCorrect, exercise.getExplanation(), correctAnswers);
		if (isCorrect)
			log.debug("Multiple choice validation: CORRECT");
		else
			log.debug("Multiple choice validation: INCORRECT - user selected {}, expected {}", userAnswers,
					correctAnswers);
		return response;
	}

	private ChoiceValidationResponseDto validateRadio(RadioExerciseDto exercise, ChoiceValidationRequestDto request) {
		ChoiceValidationResponseDto response = new ChoiceValidationResponseDto();
		List<String> userAnswers = request.getSelectedOptions();
		log.debug("Validating radio exercise with {} user answers", userAnswers.size());
		if (userAnswers.size() != 1) {
			log.debug("Radio validation: INCORRECT - expected exactly 1 answer, got {}", userAnswers.size());
			populateChoiceResponse(response, false, "Please select exactly one option", List.of());
			return response;
		}

		String userAnswer = userAnswers.get(0);
		String correctAnswer = exercise.getOptions().stream().filter(OptionDto::isCorrect).findFirst()
				.map(OptionDto::getId).orElse(null);
		log.debug("Radio validation: user selected {}, correct answer is {}", userAnswer, correctAnswer);
		if (correctAnswer == null) {
			log.error("Radio exercise has no correct answer defined: {}", exercise.getId());
			populateChoiceResponse(response, false, "Exercise configuration error - no correct answer defined.",
					List.of());
			return response;
		}
		boolean isCorrect = correctAnswer.equals(userAnswer);
		populateChoiceResponse(response, isCorrect, exercise.getExplanation(), List.of(correctAnswer));
		if (isCorrect)
			log.debug("Radio validation: CORRECT");
		else
			log.debug("Radio validation: INCORRECT");
		return response;
	}

	private void populateChoiceResponse(ChoiceValidationResponseDto response,
			boolean isCorrect,
			String explanation,
			List<String> correctAnswers) {
		response.setCorrect(isCorrect);
		response.setMessage(isCorrect ? "Helyes válasz!" : "Helytelen válasz. Próbáld újra!");
		response.setExplanation(explanation != null ? explanation : "");
		response.setCorrectAnswers(correctAnswers);
	}

	private void populateRegexResponse(RegexValidationResponseDto response,
			boolean isCorrect,
			String explanation,
			List<TestCaseResultDto> testCaseResults) {
		response.setCorrect(isCorrect);
		response.setMessage(isCorrect ? "Helyes minta!" : "Helytelen minta. Próbáld újra!");
		response.setExplanation(explanation != null ? explanation : "");
		response.setTestCaseResults(testCaseResults);
		response.setPassedTests((int) testCaseResults.stream().filter(TestCaseResultDto::isPassed).count());
		response.setTotalTests(testCaseResults.size());
	}

	private void populateXPathResponse(XPathValidationResponseDto response,
			boolean isCorrect,
			String explanationOrMessage,
			List<String> expectedNodes,
			List<String> actualNodes) {
		response.setCorrect(isCorrect);
		response.setMessage(isCorrect ? "Helyes XPath kifejezés!" : "Helytelen XPath kifejezés. Próbáld újra!");
		response.setExplanation(explanationOrMessage != null ? explanationOrMessage : "");
		response.setExpectedNodes(expectedNodes);
		response.setActualNodes(actualNodes);
	}
}
