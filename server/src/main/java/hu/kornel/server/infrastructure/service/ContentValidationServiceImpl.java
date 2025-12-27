package hu.kornel.server.infrastructure.service;

import org.springframework.stereotype.Service;

import hu.kornel.server.domain.entities.ContentType;
import hu.kornel.server.domain.service.ContentValidationService;
import hu.kornel.server.infrastructure.config.FileUploadConfig;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ContentValidationServiceImpl implements ContentValidationService {
    
    @Override
    public ValidationResult validateContent(
            String content,
            ContentType contentType,
            String originalFileName,
            long fileSizeBytes
    ) {
        log.debug("Validating content: type={}, fileName={}, size={} bytes", 
                contentType, originalFileName, fileSizeBytes);
        
        
        if (fileSizeBytes > FileUploadConfig.MAX_FILE_SIZE_BYTES) {
            String message = String.format("File size %d bytes exceeds maximum allowed size of %s",
                    fileSizeBytes, FileUploadConfig.MAX_FILE_SIZE_DISPLAY);
            log.warn("Validation failed: {}", message);
            return ValidationResult.failure(message);
        }
        
        
        if (originalFileName != null && !FileUploadConfig.hasValidExtension(originalFileName, contentType)) {
            String message = String.format("Invalid file extension for %s content type. " +
                    "Allowed extensions: %s", contentType, getAllowedExtensionsDisplay(contentType));
            log.warn("Validation failed: {}", message);
            return ValidationResult.failure(message);
        }
        
        
        if (content == null || content.isBlank()) {
            log.warn("Validation failed: Content is empty");
            return ValidationResult.failure("Content cannot be empty");
        }
        
        
        if (FileUploadConfig.containsMaliciousPatterns(content, contentType)) {
            log.warn("Validation failed: Content contains potentially malicious patterns");
            return ValidationResult.failure("Content contains potentially malicious code (scripts, event handlers, iframes). " +
                    "Please remove these elements and try again.");
        }
        
        
        String sanitizedContent = sanitizeContent(content, contentType);
        
        log.debug("Content validation successful");
        return ValidationResult.success(sanitizedContent);
    }
    
    @Override
    public boolean isContentSafe(String content, ContentType contentType) {
        if (content == null) {
            return true; 
        }
        
        return !FileUploadConfig.containsMaliciousPatterns(content, contentType);
    }
    
    private String sanitizeContent(String content, ContentType contentType) {
        if (content == null) {
            return null;
        }
        
        String sanitized = content.trim();
        return sanitized;
    }
    
    private String getAllowedExtensionsDisplay(ContentType contentType) {
        return switch (contentType) {
            case REGEX_TEXT -> String.join(", ", FileUploadConfig.ALLOWED_TEXT_EXTENSIONS);
            case HTML -> String.join(", ", FileUploadConfig.ALLOWED_HTML_EXTENSIONS);
            case XML -> String.join(", ", FileUploadConfig.ALLOWED_XML_EXTENSIONS);
        };
    }
}