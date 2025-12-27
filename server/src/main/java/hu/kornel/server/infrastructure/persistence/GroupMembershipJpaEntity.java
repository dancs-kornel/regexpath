package hu.kornel.server.infrastructure.persistence;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import hu.kornel.server.domain.entities.GroupMembership;
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
@Table(name = "group_memberships")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembershipJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private GroupJpaEntity group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private UserJpaEntity student;
    
    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    
    public static GroupMembershipJpaEntity fromDomain(GroupMembership membership) {
        return GroupMembershipJpaEntity.builder()
                .id(membership.getId())
                .groupId(membership.getGroupId())
                .studentId(membership.getStudentId())
                .joinedAt(membership.getJoinedAt())
                .build();
    }
    
    public GroupMembership toDomain() {
        return GroupMembership.builder()
                .id(this.id)
                .groupId(this.groupId)
                .studentId(this.studentId)
                .joinedAt(this.joinedAt)
                .build();
    }
}