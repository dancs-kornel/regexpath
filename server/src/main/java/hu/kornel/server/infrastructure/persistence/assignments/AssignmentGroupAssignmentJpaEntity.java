package hu.kornel.server.infrastructure.persistence.assignments;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import hu.kornel.server.domain.entities.assignments.AssignmentGroupAssignment;
import hu.kornel.server.infrastructure.persistence.GroupJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assignment_group_assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentGroupAssignmentJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private AssignmentJpaEntity assignment;
    
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private GroupJpaEntity group;
    
    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;
    
    
    public static AssignmentGroupAssignmentJpaEntity fromDomain(AssignmentGroupAssignment aga) {
        return AssignmentGroupAssignmentJpaEntity.builder()
                .id(aga.getId())
                .assignmentId(aga.getAssignmentId())
                .groupId(aga.getGroupId())
                .assignedAt(aga.getAssignedAt())
                .build();
    }
    
    public AssignmentGroupAssignment toDomain() {
        return AssignmentGroupAssignment.builder()
                .id(this.id)
                .assignmentId(this.assignmentId)
                .groupId(this.groupId)
                .assignedAt(this.assignedAt)
                .build();
    }
}