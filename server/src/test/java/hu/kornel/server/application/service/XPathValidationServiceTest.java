package hu.kornel.server.application.service;

import java.util.List;

import javax.xml.xpath.XPathExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hu.kornel.server.application.dto.validation.XPathValidationResultDto;

class XPathValidationServiceTest {

    private XPathValidationService service;

    @BeforeEach
    void setUp() {
        service = new XPathValidationService();
    }

    @Test
    @DisplayName("validateExpressionSyntax returns valid result for correct expression")
    void validateExpressionSyntax_validExpression_returnsValid() {
        XPathValidationResultDto result = service.validateExpressionSyntax("//div");
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getCompiledExpression()).isNotNull();
    }

    @Test
    @DisplayName("validateExpressionSyntax returns invalid for null or empty expression")
    void validateExpressionSyntax_invalidInputs() {
        assertThat(service.validateExpressionSyntax(null).isValid()).isFalse();
        assertThat(service.validateExpressionSyntax("   ").isValid()).isFalse();
    }

    @Test
    @DisplayName("validateExpressionSyntax rejects root-only '/'")
    void validateExpressionSyntax_rootOnly_invalid() {
        XPathValidationResultDto result = service.validateExpressionSyntax("/");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("érvénytelen kifejezés");
    }

    @Test
    @DisplayName("validateExpressionSyntax returns invalid for malformed expression")
    void validateExpressionSyntax_malformedExpression() {
        XPathValidationResultDto result = service.validateExpressionSyntax("//div[");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("hiba");
    }

    @Test
    @DisplayName("evaluateXPath returns node paths for matching nodes")
    void evaluateXPath_validExpressionAndDocument_returnsNodePaths() throws Exception {
        String xml = "<root><item>1</item><item>2</item></root>";
        XPathExpression expression = service.validateExpressionSyntax("//item").getCompiledExpression();

        List<String> nodes = service.evaluateXPath(expression, xml);

        assertThat(nodes).containsExactly("/root[1]/item[1]", "/root[1]/item[2]");
    }

    @Test
    @DisplayName("evaluateXPath throws IllegalArgumentException for empty document")
    void evaluateXPath_emptyDocument_throws() {
        XPathExpression expression = service.validateExpressionSyntax("//item").getCompiledExpression();
        assertThatThrownBy(() -> service.evaluateXPath(expression, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nem lehet üres");
    }

    @Test
    @DisplayName("evaluateXPath throws RuntimeException for malformed XML")
    void evaluateXPath_malformedXml_throws() {
        XPathExpression expression = service.validateExpressionSyntax("//item").getCompiledExpression();
        String badXml = "<root><item></root>"; // malformed
        assertThatThrownBy(() -> service.evaluateXPath(expression, badXml))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Hiba történt");
    }

    @Test
    @DisplayName("validateXPathMatch returns true for identical lists")
    void validateXPathMatch_identicalLists() {
        List<String> list = List.of("/a[1]", "/b[1]");
        assertThat(service.validateXPathMatch(list, list)).isTrue();
    }

    @Test
    @DisplayName("validateXPathMatch returns false for different order")
    void validateXPathMatch_differentOrder() {
        List<String> expected = List.of("/a[1]", "/b[1]");
        List<String> user = List.of("/b[1]", "/a[1]");
        assertThat(service.validateXPathMatch(user, expected)).isFalse();
    }

    @Test
    @DisplayName("validateXPathMatch returns false for missing or extra nodes")
    void validateXPathMatch_missingOrExtraNodes() {
        List<String> expected = List.of("/a[1]");
        List<String> user = List.of("/a[1]", "/b[1]");
        assertThat(service.validateXPathMatch(user, expected)).isFalse();
    }

    @Test
    @DisplayName("isValidDocument returns true for valid XML")
    void isValidDocument_validXml() {
        assertThat(service.isValidDocument("<root><a/></root>")).isTrue();
    }

    @Test
    @DisplayName("isValidDocument returns false for invalid XML")
    void isValidDocument_invalidXml() {
        assertThat(service.isValidDocument("<root><a></root>")).isFalse();
    }
}
