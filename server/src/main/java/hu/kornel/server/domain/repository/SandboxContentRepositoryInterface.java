package hu.kornel.server.domain.repository;

import java.util.List;
import java.util.Optional;

import hu.kornel.server.domain.entities.ContentSource;
import hu.kornel.server.domain.entities.ContentType;
import hu.kornel.server.domain.entities.SandboxContent;


public interface SandboxContentRepositoryInterface {
    SandboxContent save(SandboxContent content);
    Optional<SandboxContent> findById(Long id);
    List<SandboxContent> findByTypeAndSource(ContentType type, ContentSource source);
    List<SandboxContent> findByOwnerIdAndType(Long ownerId, ContentType type);
    List<SandboxContent> findByOwnerId(Long ownerId);
    List<SandboxContent> findBuiltInByCategory(String category);
    void deleteById(Long id);
    boolean existsById(Long id);
    long countByOwnerIdAndType(Long ownerId, ContentType type);
}