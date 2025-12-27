package hu.kornel.server.domain.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    private Long id;
    private String name;
    private String description;
    private String inviteCode;
    private Long teacherId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;

    @Builder.Default
    private Set<Long> memberIds = new HashSet<>();

    public void addMember(Long studentId) { this.memberIds.add(studentId); }
    public void removeMember(Long studentId) { this.memberIds.remove(studentId); }
    public boolean isMember(Long studentId) { return this.memberIds.contains(studentId); }
    public int getMemberCount() { return this.memberIds.size(); }
    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }
    public boolean isOwnedBy(Long userId) { return this.teacherId.equals(userId); }
}
