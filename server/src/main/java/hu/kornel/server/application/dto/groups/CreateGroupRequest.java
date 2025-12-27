package hu.kornel.server.application.dto.groups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {
    @NotBlank(message="Group name is required")
    @Size(min=3,max=100,message="Group name must be between 3 and 100 characters long")
    private String name;

    @Size(max=500, message="Description cannot exceed 500 characters")
    private String description;
}
