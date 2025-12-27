package hu.kornel.server.infrastructure.persistence.sandbox;

import hu.kornel.server.domain.entities.ContentSource;
import hu.kornel.server.domain.entities.ContentType;
import hu.kornel.server.domain.entities.SandboxContent;
import hu.kornel.server.domain.repository.SandboxContentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
@Slf4j
public class SandboxContentRepositoryImpl implements SandboxContentRepositoryInterface {
    
    private final SandboxContentJpaRepository jpaRepository;
    
    @Override
    public SandboxContent save(SandboxContent content) {
        log.debug("Saving sandbox content: {} (type: {}, source: {})", 
                content.getName(), content.getType(), content.getSource());
        
        SandboxContentJpaEntity entity = SandboxContentJpaEntity.fromDomain(content);
        SandboxContentJpaEntity savedEntity = jpaRepository.save(entity);
        
        log.debug("Saved sandbox content with ID: {}", savedEntity.getId());
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<SandboxContent> findById(Long id) {
        log.debug("Finding sandbox content by ID: {}", id);
        return jpaRepository.findById(id)
                .map(SandboxContentJpaEntity::toDomain);
    }
    
    @Override
    public List<SandboxContent> findByTypeAndSource(ContentType type, ContentSource source) {
        log.debug("Finding sandbox content by type: {} and source: {}", type, source);
        return jpaRepository.findByTypeAndSource(type, source).stream()
                .map(SandboxContentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SandboxContent> findByOwnerIdAndType(Long ownerId, ContentType type) {
        log.debug("Finding sandbox content for owner: {} and type: {}", ownerId, type);
        return jpaRepository.findByOwnerIdAndType(ownerId, type).stream()
                .map(SandboxContentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SandboxContent> findByOwnerId(Long ownerId) {
        log.debug("Finding all sandbox content for owner: {}", ownerId);
        return jpaRepository.findByOwnerId(ownerId).stream()
                .map(SandboxContentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SandboxContent> findBuiltInByCategory(String category) {
        log.debug("Finding built-in sandbox content by category: {}", category);
        return jpaRepository.findBySourceAndCategory(ContentSource.BUILTIN, category).stream()
                .map(SandboxContentJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(Long id) {
        log.debug("Deleting sandbox content by ID: {}", id);
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public long countByOwnerIdAndType(Long ownerId, ContentType type) {
        return jpaRepository.countByOwnerIdAndType(ownerId, type);
    }
}