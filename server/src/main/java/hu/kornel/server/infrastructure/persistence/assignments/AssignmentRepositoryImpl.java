package hu.kornel.server.infrastructure.persistence.assignments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentStatus;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AssignmentRepositoryImpl implements AssignmentRepositoryInterface {
    
    private final AssignmentJpaRepository jpaRepository;
    
    @Override
    public Optional<Assignment> findById(Long id) {
        return jpaRepository.findById(id)
                .map(AssignmentJpaEntity::toDomain);
    }
    
    @Override
    public List<Assignment> findByTeacherId(Long teacherId) {
        return jpaRepository.findByTeacherId(teacherId).stream()
                .map(AssignmentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Assignment> findByTeacherIdAndStatus(Long teacherId, AssignmentStatus status) {
        return jpaRepository.findByTeacherIdAndStatus(teacherId, status).stream()
                .map(AssignmentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public Assignment save(Assignment assignment) {
        AssignmentJpaEntity jpaEntity = AssignmentJpaEntity.fromDomain(assignment);
        AssignmentJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteAssignmentById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}