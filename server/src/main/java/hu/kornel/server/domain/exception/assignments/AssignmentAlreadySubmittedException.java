package hu.kornel.server.domain.exception.assignments;

public class AssignmentAlreadySubmittedException extends RuntimeException {
    public AssignmentAlreadySubmittedException(String message) {
        super(message);
    }
    
    public AssignmentAlreadySubmittedException() {
        super("A feladat már be lett nyújtva és nem szerkeszthető");
    }
}