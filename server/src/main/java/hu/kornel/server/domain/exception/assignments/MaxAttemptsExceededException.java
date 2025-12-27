package hu.kornel.server.domain.exception.assignments;

public class MaxAttemptsExceededException extends RuntimeException {
    public MaxAttemptsExceededException(String message) {
        super(message);
    }
    
    public MaxAttemptsExceededException(int maxAttempts) {
        super("Elérted a maximális próbálkozások számát: " + maxAttempts);
    }
}