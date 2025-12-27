package hu.kornel.server.application.dto.validation;

import javax.xml.xpath.XPathExpression;

import lombok.Getter;

@Getter
public class XPathValidationResultDto {
    private final boolean valid;
    private final String errorMessage;
    private final XPathExpression compiledExpression;

    private XPathValidationResultDto(boolean valid, String errorMessage, XPathExpression compiledExpression) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.compiledExpression = compiledExpression;
    }

    public boolean isInvalid() {
        return !valid;
    }

    public static XPathValidationResultDto valid(XPathExpression expression) {
        return new XPathValidationResultDto(true, null, expression);
    }

    public static XPathValidationResultDto invalid(String errorMessage) {
        return new XPathValidationResultDto(false, errorMessage, null);
    }
}
