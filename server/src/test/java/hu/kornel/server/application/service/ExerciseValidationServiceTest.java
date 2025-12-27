package hu.kornel.server.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpression;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.kornel.server.application.dto.MultipleChoiceExerciseDto;
import hu.kornel.server.application.dto.OptionDto;
import hu.kornel.server.application.dto.RadioExerciseDto;
import hu.kornel.server.application.dto.RegexSandboxExerciseDto;
import hu.kornel.server.application.dto.RegexTestCaseDto;
import hu.kornel.server.application.dto.RegexTestCasesDto;
import hu.kornel.server.application.dto.XPathSandboxExerciseDto;
import hu.kornel.server.application.dto.validation.ChoiceValidationRequestDto;
import hu.kornel.server.application.dto.validation.ChoiceValidationResponseDto;
import hu.kornel.server.application.dto.validation.PatternValidationResultDto;
import hu.kornel.server.application.dto.validation.RegexValidationRequestDto;
import hu.kornel.server.application.dto.validation.RegexValidationResponseDto;
import hu.kornel.server.application.dto.validation.TestCaseResultDto;
import hu.kornel.server.application.dto.validation.ValidationResponseDto;
import hu.kornel.server.application.dto.validation.XPathValidationRequestDto;
import hu.kornel.server.application.dto.validation.XPathValidationResponseDto;
import hu.kornel.server.application.dto.validation.XPathValidationResultDto;

@ExtendWith(MockitoExtension.class)
class ExerciseValidationServiceTest {

    @Mock
    RegexValidationService regexValidationService;

    @Mock
    XPathValidationService xpathValidationService;

    @InjectMocks
    ExerciseValidationService service;

    @Test
    @DisplayName("validateExercise: multiple choice with all correct answers returns correct response")
    void validateMultipleChoice_allCorrect() {
        MultipleChoiceExerciseDto exercise = new MultipleChoiceExerciseDto();
        exercise.setId("mc1");
        exercise.setExplanation("Great job!");
        exercise.setOptions(List.of(
                createOption("opt1", true),
                createOption("opt2", false),
                createOption("opt3", true)
        ));

        ChoiceValidationRequestDto request = new ChoiceValidationRequestDto();
        request.setSelectedOptions(List.of("opt1", "opt3"));

        ValidationResponseDto response = service.validateExercise(exercise, request);

        assertThat(response).isInstanceOf(ChoiceValidationResponseDto.class);
        ChoiceValidationResponseDto choiceResponse = (ChoiceValidationResponseDto) response;
        assertThat(choiceResponse.isCorrect()).isTrue();
        assertThat(choiceResponse.getMessage()).isEqualTo("Helyes válasz!");
        assertThat(choiceResponse.getExplanation()).isEqualTo("Great job!");
        assertThat(choiceResponse.getCorrectAnswers()).containsExactlyInAnyOrder("opt1", "opt3");
    }

    @Test
    @DisplayName("validateExercise: multiple choice with wrong answers returns incorrect response")
    void validateMultipleChoice_incorrect() {
        MultipleChoiceExerciseDto exercise = new MultipleChoiceExerciseDto();
        exercise.setId("mc2");
        exercise.setOptions(List.of(
                createOption("opt1", true),
                createOption("opt2", false)
        ));

        ChoiceValidationRequestDto request = new ChoiceValidationRequestDto();
        request.setSelectedOptions(List.of("opt2"));

        ValidationResponseDto response = service.validateExercise(exercise, request);

        ChoiceValidationResponseDto choiceResponse = (ChoiceValidationResponseDto) response;
        assertThat(choiceResponse.isCorrect()).isFalse();
        assertThat(choiceResponse.getMessage()).isEqualTo("Helytelen válasz. Próbáld újra!");
    }

    @Test
    @DisplayName("validateExercise: radio with correct answer returns correct response")
    void validateRadio_correct() {
        RadioExerciseDto exercise = new RadioExerciseDto();
        exercise.setId("radio1");
        exercise.setExplanation("Correct!");
        exercise.setOptions(List.of(
                createOption("opt1", false),
                createOption("opt2", true),
                createOption("opt3", false)
        ));

        ChoiceValidationRequestDto request = new ChoiceValidationRequestDto();
        request.setSelectedOptions(List.of("opt2"));

        ValidationResponseDto response = service.validateExercise(exercise, request);

        ChoiceValidationResponseDto choiceResponse = (ChoiceValidationResponseDto) response;
        assertThat(choiceResponse.isCorrect()).isTrue();
        assertThat(choiceResponse.getMessage()).isEqualTo("Helyes válasz!");
        assertThat(choiceResponse.getCorrectAnswers()).containsExactly("opt2");
    }

    @Test
    @DisplayName("validateExercise: radio with multiple selections returns error")
    void validateRadio_multipleSelections() {
        RadioExerciseDto exercise = new RadioExerciseDto();
        exercise.setId("radio2");
        exercise.setOptions(List.of(
                createOption("opt1", true),
                createOption("opt2", false)
        ));

        ChoiceValidationRequestDto request = new ChoiceValidationRequestDto();
        request.setSelectedOptions(List.of("opt1", "opt2"));

        ValidationResponseDto response = service.validateExercise(exercise, request);

        ChoiceValidationResponseDto choiceResponse = (ChoiceValidationResponseDto) response;
        assertThat(choiceResponse.isCorrect()).isFalse();
        assertThat(choiceResponse.getMessage()).isEqualTo("Helytelen válasz. Próbáld újra!");
    }

    @Test
    @DisplayName("validateExercise: regex sandbox with valid pattern and passing tests returns correct")
    void validateRegexSandbox_correct() {
        RegexSandboxExerciseDto exercise = new RegexSandboxExerciseDto();
        exercise.setId("regex1");
        exercise.setExplanation("Well done!");

        RegexTestCasesDto testCases = new RegexTestCasesDto();
        testCases.setPositive(List.of(
                RegexTestCaseDto.builder().text("abc").description("test1").build(),
                RegexTestCaseDto.builder().text("def").description("test2").build()
        ));
        testCases.setNegative(List.of(
                RegexTestCaseDto.builder().text("123").description("test3").build()
        ));
        exercise.setTestCases(testCases);

        RegexValidationRequestDto request = new RegexValidationRequestDto();
        request.setPattern("[a-f]+");

        Pattern compiledPattern = Pattern.compile("[a-f]+");
        PatternValidationResultDto syntaxResult = PatternValidationResultDto.valid(compiledPattern);

        List<TestCaseResultDto> positiveResults = new ArrayList<>();
        TestCaseResultDto result1 = new TestCaseResultDto();
        result1.setDescription("test1");
        result1.setPassed(true);
        result1.setPositiveCase(true);
        positiveResults.add(result1);

        List<TestCaseResultDto> negativeResults = new ArrayList<>();
        TestCaseResultDto result2 = new TestCaseResultDto();
        result2.setDescription("test3");
        result2.setPassed(true);
        result2.setPositiveCase(false);
        negativeResults.add(result2);

        when(regexValidationService.validatePatternSyntax("[a-f]+")).thenReturn(syntaxResult);
        when(regexValidationService.validatePositiveTestCases(eq(compiledPattern), anyList()))
                .thenReturn(positiveResults);
        when(regexValidationService.validateNegativeTestCases(eq(compiledPattern), anyList()))
                .thenReturn(negativeResults);

        ValidationResponseDto response = service.validateExercise(exercise, request);

        assertThat(response).isInstanceOf(RegexValidationResponseDto.class);
        RegexValidationResponseDto regexResponse = (RegexValidationResponseDto) response;
        assertThat(regexResponse.isCorrect()).isTrue();
        assertThat(regexResponse.getMessage()).isEqualTo("Helyes minta!");
        assertThat(regexResponse.getPassedTests()).isEqualTo(2);
        assertThat(regexResponse.getTotalTests()).isEqualTo(2);
        assertThat(regexResponse.getCompiledPattern()).isEqualTo("[a-f]+");
    }

    @Test
    @DisplayName("validateExercise: regex sandbox with invalid pattern syntax returns error")
    void validateRegexSandbox_invalidSyntax() {
        RegexSandboxExerciseDto exercise = new RegexSandboxExerciseDto();
        exercise.setId("regex2");

        RegexValidationRequestDto request = new RegexValidationRequestDto();
        request.setPattern("[invalid");

        PatternValidationResultDto syntaxResult = PatternValidationResultDto.invalid("Unclosed character class");

        when(regexValidationService.validatePatternSyntax("[invalid")).thenReturn(syntaxResult);

        ValidationResponseDto response = service.validateExercise(exercise, request);

        RegexValidationResponseDto regexResponse = (RegexValidationResponseDto) response;
        assertThat(regexResponse.isCorrect()).isFalse();
        assertThat(regexResponse.getMessage()).isEqualTo("Unclosed character class");
        assertThat(regexResponse.getPassedTests()).isZero();
        assertThat(regexResponse.getTotalTests()).isZero();
    }

    @Test
    @DisplayName("validateExercise: XPath sandbox with valid expression and matching nodes returns correct")
    void validateXPathSandbox_correct() throws Exception {
        XPathSandboxExerciseDto exercise = new XPathSandboxExerciseDto();
        exercise.setId("xpath1");
        exercise.setExplanation("Perfect!");
        exercise.setSampleDocument("<root><item>1</item><item>2</item></root>");
        exercise.setSolution("//item");

        XPathValidationRequestDto request = new XPathValidationRequestDto();
        request.setExpression("//item");

        XPathExpression mockExpression = org.mockito.Mockito.mock(XPathExpression.class);

        XPathValidationResultDto userSyntaxResult = XPathValidationResultDto.valid(mockExpression);
        XPathValidationResultDto solutionSyntaxResult = XPathValidationResultDto.valid(mockExpression);

        List<String> expectedNodes = List.of("<item>1</item>", "<item>2</item>");
        List<String> actualNodes = List.of("<item>1</item>", "<item>2</item>");

        when(xpathValidationService.validateExpressionSyntax("//item"))
                .thenReturn(userSyntaxResult)
                .thenReturn(solutionSyntaxResult);
        when(xpathValidationService.evaluateXPath(mockExpression, exercise.getSampleDocument()))
                .thenReturn(expectedNodes)
                .thenReturn(actualNodes);
        when(xpathValidationService.validateXPathMatch(actualNodes, expectedNodes)).thenReturn(true);

        ValidationResponseDto response = service.validateExercise(exercise, request);

        assertThat(response).isInstanceOf(XPathValidationResponseDto.class);
        XPathValidationResponseDto xpathResponse = (XPathValidationResponseDto) response;
        assertThat(xpathResponse.isCorrect()).isTrue();
        assertThat(xpathResponse.getMessage()).isEqualTo("Helyes XPath kifejezés!");
        assertThat(xpathResponse.getExplanation()).isEqualTo("Perfect!");
    }

    @Test
    @DisplayName("validateExercise: unsupported exercise type returns error response")
    void validateExercise_unsupportedType() {
        MultipleChoiceExerciseDto exercise = new MultipleChoiceExerciseDto();
        exercise.setId("mc3");

        RegexValidationRequestDto wrongRequest = new RegexValidationRequestDto();

        ValidationResponseDto response = service.validateExercise(exercise, wrongRequest);

        assertThat(response).isInstanceOf(RegexValidationResponseDto.class);
        assertThat(response.isCorrect()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Unsupported exercise type");
    }

    private OptionDto createOption(String id, boolean correct) {
        OptionDto option = new OptionDto();
        option.setId(id);
        option.setCorrect(correct);
        option.setText("Option " + id);
        return option;
    }
}
