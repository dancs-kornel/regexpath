package hu.kornel.server.domain.exception;

public class AlreadyMemberException extends RuntimeException {
    public AlreadyMemberException(Long studentId, Long groupId) {
        super("Student " + studentId + " is already a member of group " + groupId);
    }
}
