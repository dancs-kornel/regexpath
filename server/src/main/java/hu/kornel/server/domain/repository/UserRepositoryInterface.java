package hu.kornel.server.domain.repository;

import java.util.Optional;

import hu.kornel.server.domain.entities.User;

public interface UserRepositoryInterface {
   Optional<User> findByEmail(String email);
   Optional<User> findByUsername(String username);
   Optional<User> findById(Long id);
   User save(User user);
   boolean existsByEmail(String email);
   boolean existsByUsername(String username);
   void deleteById(Long id);
}
