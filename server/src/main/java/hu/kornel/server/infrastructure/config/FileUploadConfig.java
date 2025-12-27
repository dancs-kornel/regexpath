package hu.kornel.server.infrastructure.config;

import java.util.Set;
import java.util.regex.Pattern;

import hu.kornel.server.domain.entities.ContentType;


public class FileUploadConfig {
    
    
    public static final long MAX_FILE_SIZE_BYTES = 1_048_576; 
    public static final String MAX_FILE_SIZE_DISPLAY = "1MB";
    
    
    public static final Set<String> ALLOWED_TEXT_MIME_TYPES = Set.of(
            "text/plain",
            "text/txt",
            "application/octet-stream" 
    );
    
    public static final Set<String> ALLOWED_HTML_MIME_TYPES = Set.of(
            "text/html",
            "application/xhtml+xml",
            "text/plain" 
    );
    
    public static final Set<String> ALLOWED_XML_MIME_TYPES = Set.of(
            "application/xml",
            "text/xml",
            "application/xhtml+xml",
            "text/plain" 
    );
    
    
    public static final Set<String> ALLOWED_TEXT_EXTENSIONS = Set.of(
            ".txt"
    );
    
    public static final Set<String> ALLOWED_HTML_EXTENSIONS = Set.of(
            ".html", ".htm"
    );
    
    public static final Set<String> ALLOWED_XML_EXTENSIONS = Set.of(
            ".xml"
    );
    
    
    private static final Pattern SCRIPT_TAG_PATTERN = 
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern JAVASCRIPT_PROTOCOL_PATTERN = 
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern EVENT_HANDLER_PATTERN = 
            Pattern.compile("\\bon\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern IFRAME_TAG_PATTERN = 
            Pattern.compile("<iframe[^>]*>", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern OBJECT_EMBED_PATTERN = 
            Pattern.compile("<(object|embed)[^>]*>", Pattern.CASE_INSENSITIVE);
    
    
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "untitled";
        }
        
        
        fileName = fileName.replaceAll("[./\\\\]", "_");
        
        
        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        
        if (fileName.length() > 100) {
            
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0 && lastDot < fileName.length() - 1) {
                String extension = fileName.substring(lastDot);
                String name = fileName.substring(0, Math.min(95, lastDot));
                fileName = name + extension;
            } else {
                fileName = fileName.substring(0, 100);
            }
        }
        
        return fileName;
    }
    
    
    public static boolean isAllowedMimeType(String mimeType, ContentType contentType) {
        if (mimeType == null || contentType == null) {
            return false;
        }
        
        return switch (contentType) {
            case REGEX_TEXT -> ALLOWED_TEXT_MIME_TYPES.contains(mimeType.toLowerCase());
            case HTML -> ALLOWED_HTML_MIME_TYPES.contains(mimeType.toLowerCase());
            case XML -> ALLOWED_XML_MIME_TYPES.contains(mimeType.toLowerCase());
        };
    }
    
    
    public static boolean hasValidExtension(String fileName, ContentType contentType) {
        if (fileName == null || contentType == null) {
            return false;
        }
        
        String lowerFileName = fileName.toLowerCase();
        
        return switch (contentType) {
            case REGEX_TEXT -> ALLOWED_TEXT_EXTENSIONS.stream()
                    .anyMatch(lowerFileName::endsWith);
            case HTML -> ALLOWED_HTML_EXTENSIONS.stream()
                    .anyMatch(lowerFileName::endsWith);
            case XML -> ALLOWED_XML_EXTENSIONS.stream()
                    .anyMatch(lowerFileName::endsWith);
        };
    }
    
    
    public static boolean containsMaliciousPatterns(String content, ContentType contentType) {
        if (content == null) {
            return false;
        }
        
        if (contentType != ContentType.HTML) {
            return false;
        }
        
        return SCRIPT_TAG_PATTERN.matcher(content).find() ||
               JAVASCRIPT_PROTOCOL_PATTERN.matcher(content).find() ||
               EVENT_HANDLER_PATTERN.matcher(content).find() ||
               IFRAME_TAG_PATTERN.matcher(content).find() ||
               OBJECT_EMBED_PATTERN.matcher(content).find();
    }
}