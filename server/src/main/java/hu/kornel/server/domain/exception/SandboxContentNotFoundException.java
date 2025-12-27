package hu.kornel.server.domain.exception;


public class SandboxContentNotFoundException extends RuntimeException {
    
    private final Long contentId;
    
    public SandboxContentNotFoundException(Long contentId) {
        super(String.format("Sandbox content with ID %d not found", contentId));
        this.contentId = contentId;
    }
    
    public Long getContentId() {
        return contentId;
    }
}