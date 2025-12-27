package hu.kornel.server.infrastructure.persistence.assignments;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.ExerciseAnswer;
import hu.kornel.server.domain.repository.assignments.ExerciseAnswerRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ExerciseAnswerRepositoryImpl implements ExerciseAnswerRepositoryInterface {
    
    private final ExerciseAnswerJpaRepository jpaRepository;
    
    @Override
    public Optional<ExerciseAnswer> findById(Long id) {
        return jpaRepository.findById(id)
                .map(ExerciseAnswerJpaEntity::toDomain);
    }
    
    @Override
    public List<ExerciseAnswer> findByAttemptId(Long attemptId) {
        return jpaRepository.findByAttemptId(attemptId).stream()
                .map(ExerciseAnswerJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ExerciseAnswer save(ExerciseAnswer answer) {
        ExerciseAnswerJpaEntity jpaEntity = ExerciseAnswerJpaEntity.fromDomain(answer);
        ExerciseAnswerJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteByAttemptId(Long attemptId) {
        jpaRepository.deleteByAttemptId(attemptId);
    }
}