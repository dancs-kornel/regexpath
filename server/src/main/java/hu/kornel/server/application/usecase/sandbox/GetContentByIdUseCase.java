package hu.kornel.server.application.usecase.sandbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.sandbox.SandboxContentDto;
import hu.kornel.server.application.dto.sandbox.SandboxContentMapper;
import hu.kornel.server.domain.entities.SandboxContent;
import hu.kornel.server.domain.exception.SandboxContentNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.SandboxContentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class GetContentByIdUseCase {
    
    private final SandboxContentRepositoryInterface sandboxContentRepository;
    
    @Transactional(readOnly = true)
    public SandboxContentDto execute(Long contentId, Long userId) {
        log.debug("Getting content by id: {} for user: {}", contentId, userId);
        
        SandboxContent content = sandboxContentRepository.findById(contentId)
                .orElseThrow(() -> new SandboxContentNotFoundException(contentId));
        
        if (userId == null) {
            
            if (!content.isBuiltIn()) {
                log.warn("Guest user tried to access non-builtin content: contentId={}", contentId);
                throw new UnauthorizedAccessException("You must be logged in to access this content");
            }
        } else {
            
            if (!content.isBuiltIn() && !content.isOwnedBy(userId)) {
                log.warn("Unauthorized access attempt: userId {} tried to access content {} owned by {}", 
                        userId, contentId, content.getOwnerId());
                throw new UnauthorizedAccessException("You do not have permission to access this content");
            }
        }
        
        log.debug("Successfully retrieved content: {}", contentId);
        return SandboxContentMapper.toDto(content);
    }
}