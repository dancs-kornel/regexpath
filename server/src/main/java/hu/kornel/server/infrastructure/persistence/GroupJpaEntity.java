package hu.kornel.server.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import hu.kornel.server.domain.entities.Group;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "invite_code", nullable = false, unique = true, length = 6)
    private String inviteCode;
    
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;
    
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<GroupMembershipJpaEntity> memberships = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private UserJpaEntity teacher;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
    
    
    public static GroupJpaEntity fromDomain(Group group) {
        return GroupJpaEntity.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .inviteCode(group.getInviteCode())
                .teacherId(group.getTeacherId())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .active(group.isActive())
                .build();
    }
    
    public Group toDomain() {
        
        
        return Group.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .inviteCode(this.inviteCode)
                .teacherId(this.teacherId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .active(this.active)
                .memberIds(new HashSet<>())  
                .build();
    }
}