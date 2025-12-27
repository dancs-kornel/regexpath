package hu.kornel.server.domain.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembership {
    private Long id;
    private Long groupId;
    private Long studentId;
    private LocalDateTime joinedAt;
}
