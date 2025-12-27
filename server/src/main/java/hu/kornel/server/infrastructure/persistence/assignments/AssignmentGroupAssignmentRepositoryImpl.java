package hu.kornel.server.infrastructure.persistence.assignments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.AssignmentGroupAssignment;
import hu.kornel.server.domain.repository.assignments.AssignmentGroupAssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AssignmentGroupAssignmentRepositoryImpl implements AssignmentGroupAssignmentRepositoryInterface {
    
    private final AssignmentGroupAssignmentJpaRepository jpaRepository;
    
    @Override
    public Optional<AssignmentGroupAssignment> findById(Long id) {
        return jpaRepository.findById(id)
                .map(AssignmentGroupAssignmentJpaEntity::toDomain);
    }
    
    @Override
    public Optional<AssignmentGroupAssignment> findByAssignmentIdAndGroupId(Long assignmentId, Long groupId) {
        return jpaRepository.findByAssignmentIdAndGroupId(assignmentId, groupId)
                .map(AssignmentGroupAssignmentJpaEntity::toDomain);
    }
    
    @Override
    public List<AssignmentGroupAssignment> findByAssignmentId(Long assignmentId) {
        return jpaRepository.findByAssignmentId(assignmentId).stream()
                .map(AssignmentGroupAssignmentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AssignmentGroupAssignment> findByGroupId(Long groupId) {
        return jpaRepository.findByGroupId(groupId).stream()
                .map(AssignmentGroupAssignmentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public AssignmentGroupAssignment save(AssignmentGroupAssignment assignmentGroupAssignment) {
        AssignmentGroupAssignmentJpaEntity jpaEntity = 
                AssignmentGroupAssignmentJpaEntity.fromDomain(assignmentGroupAssignment);
        AssignmentGroupAssignmentJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteByAssignmentIdAndGroupId(Long assignmentId, Long groupId) {
        jpaRepository.deleteByAssignmentIdAndGroupId(assignmentId, groupId);
    }
    
    @Override
    public boolean existsByAssignmentIdAndGroupId(Long assignmentId, Long groupId) {
        return jpaRepository.existsByAssignmentIdAndGroupId(assignmentId, groupId);
    }
}