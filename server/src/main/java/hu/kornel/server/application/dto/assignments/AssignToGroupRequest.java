package hu.kornel.server.application.dto.assignments;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignToGroupRequest {
    @NotEmpty(message="Legalább egy csoport megadása kötelező")
    private List<@NotNull Long> groupIds;
}
