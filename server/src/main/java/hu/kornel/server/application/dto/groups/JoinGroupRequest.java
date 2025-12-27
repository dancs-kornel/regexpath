package hu.kornel.server.application.dto.groups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupRequest {
    @NotBlank(message="Invite code is required")
    @Pattern(regexp="^[A-Z0-9]{6}$", message="Invite code must be 6 alphanumeric characters")
    private String inviteCode;
}
