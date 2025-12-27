package hu.kornel.server.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, Long> {
    Optional<GroupJpaEntity> findByInviteCode(String inviteCode);
    
    List<GroupJpaEntity> findByTeacherId(Long teacherId);
    
    @Query("SELECT DISTINCT g FROM GroupJpaEntity g LEFT JOIN FETCH g.memberships m WHERE m.studentId = :studentId")
    List<GroupJpaEntity> findGroupsByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("DELETE FROM GroupJpaEntity g WHERE g.id = :groupId")
    void deleteGroupById(@Param("groupId") Long groupId);
    
    boolean existsByInviteCode(String inviteCode);
}