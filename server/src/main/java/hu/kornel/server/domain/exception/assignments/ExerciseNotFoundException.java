package hu.kornel.server.domain.exception.assignments;

public class ExerciseNotFoundException extends RuntimeException {
    
    public ExerciseNotFoundException(Long exerciseId) {
        super(String.format("A gyakorlat nem található: %d", exerciseId));
    }
}