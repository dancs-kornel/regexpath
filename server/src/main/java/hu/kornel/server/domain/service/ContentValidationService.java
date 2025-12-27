package hu.kornel.server.domain.service;

import hu.kornel.server.domain.entities.ContentType;


public interface ContentValidationService {

    record ValidationResult(boolean isValid, String sanitizedContent, String errorMessage) {
        public static ValidationResult success(String sanitizedContent) {
            return new ValidationResult(true, sanitizedContent, null);
        }
        
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, null, errorMessage);
        }
    }

    ValidationResult validateContent(String content, ContentType contentType, String originalFileName, long fileSizeBytes);
    
    boolean isContentSafe(String content, ContentType contentType);
}