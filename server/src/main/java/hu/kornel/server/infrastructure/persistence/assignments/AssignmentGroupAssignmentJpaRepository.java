package hu.kornel.server.infrastructure.persistence.assignments;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentGroupAssignmentJpaRepository extends JpaRepository<AssignmentGroupAssignmentJpaEntity, Long> {
    
    Optional<AssignmentGroupAssignmentJpaEntity> findByAssignmentIdAndGroupId(Long assignmentId, Long groupId);
    
    List<AssignmentGroupAssignmentJpaEntity> findByAssignmentId(Long assignmentId);
    
    List<AssignmentGroupAssignmentJpaEntity> findByGroupId(Long groupId);
    
    @Modifying
    @Query("DELETE FROM AssignmentGroupAssignmentJpaEntity a WHERE a.assignmentId = :assignmentId AND a.groupId = :groupId")
    void deleteByAssignmentIdAndGroupId(@Param("assignmentId") Long assignmentId, @Param("groupId") Long groupId);
    
    boolean existsByAssignmentIdAndGroupId(Long assignmentId, Long groupId);
}