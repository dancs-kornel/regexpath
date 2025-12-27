package hu.kornel.server.infrastructure.persistence.assignments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.repository.assignments.ExerciseRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ExerciseRepositoryImpl implements ExerciseRepositoryInterface {
    
    private final ExerciseJpaRepository jpaRepository;
    
    @Override
    public Optional<Exercise> findById(Long id) {
        return jpaRepository.findById(id)
                .map(ExerciseJpaEntity::toDomain);
    }
    
    @Override
    public List<Exercise> findByAssignmentId(Long assignmentId) {
        return jpaRepository.findByAssignmentId(assignmentId).stream()
                .map(ExerciseJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Exercise> findByAssignmentIdOrderByOrderIndex(Long assignmentId) {
        return jpaRepository.findByAssignmentIdOrderByOrderIndexAsc(assignmentId).stream()
                .map(ExerciseJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public Exercise save(Exercise exercise) {
        ExerciseJpaEntity jpaEntity = ExerciseJpaEntity.fromDomain(exercise);
        ExerciseJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteByAssignmentId(Long assignmentId) {
        jpaRepository.deleteByAssignmentId(assignmentId);
    }
}