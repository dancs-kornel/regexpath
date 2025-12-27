package hu.kornel.server.domain.entities.assignments;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentGroupAssignment {
    private Long id;
    private Long assignmentId;
    private Long groupId;
    private LocalDateTime assignedAt;

    public boolean isAssignedToGroup(Long groupId) { return this.groupId.equals(groupId); }
}
