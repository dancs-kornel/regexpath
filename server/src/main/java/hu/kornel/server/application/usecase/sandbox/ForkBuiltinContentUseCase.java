package hu.kornel.server.application.usecase.sandbox;

import hu.kornel.server.application.dto.sandbox.ForkContentRequest;
import hu.kornel.server.application.dto.sandbox.SandboxContentDto;
import hu.kornel.server.application.dto.sandbox.SandboxContentMapper;
import hu.kornel.server.domain.entities.SandboxContent;
import hu.kornel.server.domain.exception.InvalidContentException;
import hu.kornel.server.domain.exception.SandboxContentNotFoundException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.SandboxContentRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.service.ContentValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ForkBuiltinContentUseCase {
    
    private final SandboxContentRepositoryInterface sandboxContentRepository;
    private final UserRepositoryInterface userRepository;
    private final ContentValidationService validationService;
    
    @Transactional
    public SandboxContentDto execute(
            Long builtinContentId,
            ForkContentRequest request,
            Long userId
    ) {
        log.debug("Forking built-in content: contentId={}, userId={}", builtinContentId, userId);
        
        
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        
        SandboxContent original = sandboxContentRepository.findById(builtinContentId)
                .orElseThrow(() -> new SandboxContentNotFoundException(builtinContentId));
        
        
        if (!original.isBuiltIn()) {
            log.warn("Attempted to fork non-builtin content: contentId={}, userId={}", 
                    builtinContentId, userId);
            throw new IllegalArgumentException("Can only fork built-in content");
        }
        
        
        if (!validationService.isContentSafe(request.getContent(), original.getType())) {
            log.warn("Fork attempt with unsafe content: contentId={}, userId={}", 
                    builtinContentId, userId);
            throw new InvalidContentException(
                    "Content contains potentially malicious code. Please remove scripts, " +
                    "event handlers, and iframes before saving.");
        }
        
        
        SandboxContent fork = original.createFork(userId);
        
        
        fork.setContent(request.getContent().trim());
        
        
        if (request.getName() != null && !request.getName().isBlank()) {
            fork.setName(request.getName());
        }
        
        
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            fork.setDescription(request.getDescription());
        }
        
        
        SandboxContent saved = sandboxContentRepository.save(fork);
        
        log.info("Successfully forked content: originalId={}, forkId={}, userId={}", 
                builtinContentId, saved.getId(), userId);
        
        return SandboxContentMapper.toDto(saved);
    }
}