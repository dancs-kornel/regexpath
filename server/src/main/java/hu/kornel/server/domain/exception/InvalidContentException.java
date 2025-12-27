package hu.kornel.server.domain.exception;


public class InvalidContentException extends RuntimeException {
    
    public InvalidContentException(String message) {
        super(message);
    }
    
    public InvalidContentException(String message, Throwable cause) {
        super(message, cause);
    }
}