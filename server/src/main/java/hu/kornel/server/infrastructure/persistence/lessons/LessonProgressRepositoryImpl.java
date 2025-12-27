package hu.kornel.server.infrastructure.persistence.lessons;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import hu.kornel.server.domain.entities.LessonProgress;
import hu.kornel.server.domain.repository.LessonProgressRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LessonProgressRepositoryImpl implements LessonProgressRepositoryInterface {
    
    private final LessonProgressJpaRepository jpaRepository;
    
    @Override
    public Optional<LessonProgress> findByUserIdAndLessonId(Long userId, String lessonId) {
        return jpaRepository.findByUserIdAndLessonId(userId, lessonId)
                .map(LessonProgressJpaEntity::toDomain);
    }
    
    @Override
    public List<LessonProgress> findAllByUserId(Long userId) {
        return jpaRepository.findAllByUserId(userId).stream()
                .map(LessonProgressJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LessonProgress> findCompletedByUserId(Long userId) {
        return jpaRepository.findAllByUserIdAndCompletedTrue(userId).stream()
                .map(LessonProgressJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<LessonProgress> findLastAccessedByUserId(Long userId) {
        return jpaRepository.findLastAccessedByUserId(userId)
                .map(LessonProgressJpaEntity::toDomain);
    }
    
    @Override
    public LessonProgress save(LessonProgress lessonProgress) {
        LessonProgressJpaEntity jpaEntity = LessonProgressJpaEntity.fromDomain(lessonProgress);
        LessonProgressJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    public boolean existsByUserIdAndLessonId(Long userId, String lessonId) {
        return jpaRepository.existsByUserIdAndLessonId(userId, lessonId);
    }
    
    @Override
    public void deleteByUserIdAndLessonId(Long userId, String lessonId) {
        jpaRepository.deleteByUserIdAndLessonId(userId, lessonId);
    }
}