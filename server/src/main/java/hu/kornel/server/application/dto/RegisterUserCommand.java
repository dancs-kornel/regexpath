package hu.kornel.server.application.dto;

import hu.kornel.server.domain.entities.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserCommand {
    private String username;
    private String email;
    private String password;
    private UserRole role; 
}
