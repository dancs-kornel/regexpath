package hu.kornel.server.domain.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public static UserAlreadyExistsException withEmail(String email) {
        return new UserAlreadyExistsException(
            String.format("User with email '%s' already exists", email)
        );
    }
    
    public static UserAlreadyExistsException withUsername(String username) {
        return new UserAlreadyExistsException(
            String.format("User with username '%s' already exists", username)
        );
    }
}
