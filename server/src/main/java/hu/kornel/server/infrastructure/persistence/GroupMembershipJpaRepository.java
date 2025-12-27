package hu.kornel.server.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMembershipJpaRepository extends JpaRepository<GroupMembershipJpaEntity, Long> {
    Optional<GroupMembershipJpaEntity> findByGroupIdAndStudentId(Long groupId, Long studentId);
    
    List<GroupMembershipJpaEntity> findByGroupId(Long groupId);
    
    List<GroupMembershipJpaEntity> findByStudentId(Long studentId);
    
    void deleteByGroupIdAndStudentId(Long groupId, Long studentId);
    
    boolean existsByGroupIdAndStudentId(Long groupId, Long studentId);
}