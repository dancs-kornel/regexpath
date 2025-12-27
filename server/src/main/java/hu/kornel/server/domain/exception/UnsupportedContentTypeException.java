package hu.kornel.server.domain.exception;


public class UnsupportedContentTypeException extends RuntimeException {
    
    private final String contentType;
    
    public UnsupportedContentTypeException(String contentType) {
        super(String.format("Content type '%s' is not supported", contentType));
        this.contentType = contentType;
    }
    
    public String getContentType() {
        return contentType;
    }
}