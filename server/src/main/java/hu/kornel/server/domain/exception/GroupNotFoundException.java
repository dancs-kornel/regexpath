package hu.kornel.server.domain.exception;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(Long groupId) {
        super("Group not found with id: " + groupId);
    }
    
    public GroupNotFoundException(String inviteCode) {
        super("Group not found with invite code: " + inviteCode);
    }
}

