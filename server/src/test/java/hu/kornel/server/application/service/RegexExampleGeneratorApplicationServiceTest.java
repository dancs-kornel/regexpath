package hu.kornel.server.application.service;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import hu.kornel.server.application.dto.sandbox.RegexExampleDto;
import hu.kornel.server.application.dto.sandbox.RegexExampleRequestDto;
import hu.kornel.server.application.dto.sandbox.RegexExampleResponseDto;
import hu.kornel.server.domain.service.RegexExampleGeneratorServiceInterface;
import hu.kornel.server.domain.service.RegexExampleGeneratorServiceInterface.NegativeExample;
import hu.kornel.server.domain.service.RegexParserServiceInterface;
import hu.kornel.server.domain.valueObjects.regex.RegexNode;

class RegexExampleGeneratorApplicationServiceTest {

    @Mock
    private RegexParserServiceInterface parserService;
    @Mock
    private RegexExampleGeneratorServiceInterface exampleGeneratorService;

    @InjectMocks
    private RegexExampleGeneratorApplicationService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("returns positives and negatives; applies flags and default limits (5/3)")
    void generateExamples_success() {
        RegexExampleRequestDto req = new RegexExampleRequestDto("abc+", true, false);

        RegexNode ast = mock(RegexNode.class);
        when(parserService.parse("abc+")).thenReturn(ast);

        ArgumentCaptor<Pattern> patternCaptor = ArgumentCaptor.forClass(Pattern.class);

        when(exampleGeneratorService.generatePositiveExamples(eq(ast), patternCaptor.capture(), eq(5)))
                .thenReturn(List.of("abc", "abcc", "abccc"));

        when(exampleGeneratorService.generateNegativeExamples(eq(ast), any(Pattern.class), eq(3)))
                .thenReturn(List.of(
                        new NegativeExample("ab", "too short"),
                        new NegativeExample("xabc", "prefix mismatch")));

        RegexExampleResponseDto resp = service.generateExamples(req);

        assertThat(resp.getErrorMessage()).isNull();
        assertThat(resp.getPositiveExamples())
                .extracting(RegexExampleDto::getText)
                .containsExactly("abc", "abcc", "abccc");
        assertThat(resp.getNegativeExamples())
                .extracting(RegexExampleDto::getText)
                .containsExactly("ab", "xabc");

        Pattern compiled = patternCaptor.getValue();
        assertThat(compiled.flags() & Pattern.CASE_INSENSITIVE).isNotZero();
        assertThat(compiled.flags() & Pattern.MULTILINE).isZero();

        verify(parserService).parse("abc+");
        verify(exampleGeneratorService).generatePositiveExamples(eq(ast), any(Pattern.class), eq(5));
        verify(exampleGeneratorService).generateNegativeExamples(eq(ast), any(Pattern.class), eq(3));
        verifyNoMoreInteractions(parserService, exampleGeneratorService);
    }

    @Test
    @DisplayName("invalid regex pattern -> returns error without calling domain services")
    void generateExamples_invalidPattern_returnsError() {
        RegexExampleRequestDto req = new RegexExampleRequestDto("(", false, false);

        RegexExampleResponseDto resp = service.generateExamples(req);

        assertThat(resp.getErrorMessage()).isNotNull();
        assertThat(resp.getErrorMessage()).contains("Invalid regex pattern");
        assertThat(resp.getPositiveExamples()).isEmpty();
        assertThat(resp.getNegativeExamples()).isEmpty();

        verifyNoInteractions(parserService, exampleGeneratorService);
    }

    @DisplayName("invalid pattern is rejected before parser is called")
    @Test
    void generateExamples_invalidPattern_beforeParser() {
        RegexExampleRequestDto req = new RegexExampleRequestDto("\\K", false, false);

        RegexExampleResponseDto resp = service.generateExamples(req);

        assertThat(resp.getErrorMessage()).isNotNull();
        assertThat(resp.getErrorMessage()).contains("Invalid regex pattern");
        verifyNoInteractions(parserService, exampleGeneratorService);
    }

    @Test
    @DisplayName("unexpected exception from generator -> generic error response")
    void generateExamples_generatorThrows_returnsUnexpectedError() {
        RegexExampleRequestDto req = new RegexExampleRequestDto("a+", false, true);

        RegexNode ast = mock(RegexNode.class);
        when(parserService.parse("a+")).thenReturn(ast);

        when(exampleGeneratorService.generatePositiveExamples(any(), any(), eq(5)))
                .thenThrow(new RuntimeException("boom"));

        RegexExampleResponseDto resp = service.generateExamples(req);

        assertThat(resp.getErrorMessage()).isNotNull();
        assertThat(resp.getErrorMessage()).contains("Unexpected error");
        assertThat(resp.getPositiveExamples()).isEmpty();
        assertThat(resp.getNegativeExamples()).isEmpty();

        verify(parserService).parse("a+");
        verify(exampleGeneratorService).generatePositiveExamples(any(), any(), eq(5));
        verify(exampleGeneratorService, never()).generateNegativeExamples(any(), any(), anyInt());
    }

    @Test
    @DisplayName("applies multiline flag when requested")
    void generateExamples_appliesMultilineFlag() {
        RegexExampleRequestDto req = new RegexExampleRequestDto("^abc$", false, true);

        RegexNode ast = mock(RegexNode.class);
        when(parserService.parse("^abc$")).thenReturn(ast);

        ArgumentCaptor<Pattern> patternCaptor = ArgumentCaptor.forClass(Pattern.class);

        when(exampleGeneratorService.generatePositiveExamples(eq(ast), patternCaptor.capture(), eq(5)))
                .thenReturn(List.of("abc"));
        when(exampleGeneratorService.generateNegativeExamples(eq(ast), any(Pattern.class), eq(3)))
                .thenReturn(List.of());

        RegexExampleResponseDto resp = service.generateExamples(req);

        assertThat(resp.getErrorMessage()).isNull(); 
        Pattern compiled = patternCaptor.getValue();
        assertThat(compiled.flags() & Pattern.MULTILINE).isNotZero();

        verify(parserService).parse("^abc$");
        verify(exampleGeneratorService).generatePositiveExamples(eq(ast), any(Pattern.class), eq(5));
        verify(exampleGeneratorService).generateNegativeExamples(eq(ast), any(Pattern.class), eq(3));
    }
}
