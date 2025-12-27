package hu.kornel.server.domain.exception;

public class LessonProgressNotFoundException extends RuntimeException {
    public LessonProgressNotFoundException(String message) {
        super(message);
    }
}