package hu.kornel.server.domain.exception;

public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static InvalidTokenException expired() {
        return new InvalidTokenException("Token has expired");
    }
    
    public static InvalidTokenException malformed() {
        return new InvalidTokenException("Token is malformed");
    }
    
    public static InvalidTokenException invalid() {
        return new InvalidTokenException("Token is invalid");
    }
}