package hu.kornel.server.infrastructure.persistence.assignments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AssignmentAttemptRepositoryImpl implements AssignmentAttemptRepositoryInterface {
    
    private final AssignmentAttemptJpaRepository jpaRepository;
    
    @Override
    public Optional<AssignmentAttempt> findById(Long id) {
        return jpaRepository.findById(id)
                .map(AssignmentAttemptJpaEntity::toDomain);
    }
    
    @Override
    public List<AssignmentAttempt> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId) {
        return jpaRepository.findByAssignmentIdAndStudentId(assignmentId, studentId).stream()
                .map(AssignmentAttemptJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AssignmentAttempt> findByStudentId(Long studentId) {
        return jpaRepository.findByStudentId(studentId).stream()
                .map(AssignmentAttemptJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AssignmentAttempt> findByAssignmentIdAndGroupId(Long assignmentId, Long groupId) {
        return jpaRepository.findByAssignmentIdAndGroupId(assignmentId, groupId).stream()
                .map(AssignmentAttemptJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<AssignmentAttempt> findByAssignmentIdAndStudentIdAndAttemptNumber(
            Long assignmentId, Long studentId, Integer attemptNumber) {
        return jpaRepository.findByAssignmentIdAndStudentIdAndAttemptNumber(
                assignmentId, studentId, attemptNumber)
                .map(AssignmentAttemptJpaEntity::toDomain);
    }
    
    @Override
    public int countByAssignmentIdAndStudentId(Long assignmentId, Long studentId) {
        return jpaRepository.countByAssignmentIdAndStudentId(assignmentId, studentId);
    }
    
    @Override
    @Transactional
    public AssignmentAttempt save(AssignmentAttempt attempt) {
        AssignmentAttemptJpaEntity jpaEntity = AssignmentAttemptJpaEntity.fromDomain(attempt);
        AssignmentAttemptJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}