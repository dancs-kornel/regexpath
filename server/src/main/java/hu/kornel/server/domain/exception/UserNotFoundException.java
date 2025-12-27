package hu.kornel.server.domain.exception;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public static UserNotFoundException withId(Long id) {
        return new UserNotFoundException(
            String.format("User with ID %d not found", id)
        );
    }
    
    public static UserNotFoundException withEmail(String email) {
        return new UserNotFoundException(
            String.format("User with email '%s' not found", email)
        );
    }
    
    public static UserNotFoundException withUsername(String username) {
        return new UserNotFoundException(
            String.format("User with username '%s' not found", username)
        );
    }
}