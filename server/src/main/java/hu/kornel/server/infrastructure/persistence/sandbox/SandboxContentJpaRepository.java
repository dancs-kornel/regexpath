package hu.kornel.server.infrastructure.persistence.sandbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.kornel.server.domain.entities.ContentSource;
import hu.kornel.server.domain.entities.ContentType;


@Repository
public interface SandboxContentJpaRepository extends JpaRepository<SandboxContentJpaEntity, Long> {
    List<SandboxContentJpaEntity> findByTypeAndSource(ContentType type, ContentSource source);
    List<SandboxContentJpaEntity> findByOwnerIdAndType(Long ownerId, ContentType type);
    List<SandboxContentJpaEntity> findByOwnerId(Long ownerId);
    List<SandboxContentJpaEntity> findBySourceAndCategory(ContentSource source, String category);
    long countByOwnerIdAndType(Long ownerId, ContentType type);
}