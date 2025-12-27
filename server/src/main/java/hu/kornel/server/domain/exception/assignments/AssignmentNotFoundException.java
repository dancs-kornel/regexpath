package hu.kornel.server.domain.exception.assignments;

public class AssignmentNotFoundException extends RuntimeException {
    public AssignmentNotFoundException(String message) {
        super(message);
    }
    
    public AssignmentNotFoundException(Long assignmentId) {
        super("Feladat nem található: " + assignmentId);
    }
}