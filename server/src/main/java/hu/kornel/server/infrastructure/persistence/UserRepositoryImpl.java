package hu.kornel.server.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryInterface {
    
    private final UserJpaRepository jpaRepository;
    
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
                .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity = UserJpaEntity.fromDomain(user);
        UserJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}