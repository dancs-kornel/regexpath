package hu.kornel.server.application.usecase.sandbox;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.sandbox.SandboxContentDto;
import hu.kornel.server.application.dto.sandbox.SandboxContentMapper;
import hu.kornel.server.application.dto.sandbox.UpdateContentRequest;
import hu.kornel.server.domain.entities.SandboxContent;
import hu.kornel.server.domain.exception.InvalidContentException;
import hu.kornel.server.domain.exception.SandboxContentNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.SandboxContentRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.service.ContentValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUserContentUseCase {
    
    private final SandboxContentRepositoryInterface sandboxContentRepository;
    private final UserRepositoryInterface userRepository;
    private final ContentValidationService validationService;
    
    @Transactional
    public SandboxContentDto execute(
            Long contentId,
            UpdateContentRequest request,
            Long userId
    ) {
        log.debug("Updating content: contentId={}, userId={}", contentId, userId);
        
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        SandboxContent content = sandboxContentRepository.findById(contentId)
                .orElseThrow(() -> new SandboxContentNotFoundException(contentId));
        
        
        if (!content.canBeEditedBy(userId)) {
            log.warn("Unauthorized update attempt: userId {} tried to update content {} owned by {}", 
                    userId, contentId, content.getOwnerId());
            
            if (content.isBuiltIn()) {
                throw new UnauthorizedAccessException(
                        "Cannot edit built-in content directly. Please fork it first.");
            } else {
                throw new UnauthorizedAccessException(
                        "You do not have permission to edit this content");
            }
        }
        
        if (!validationService.isContentSafe(request.getContent(), content.getType())) {
            log.warn("Update attempt with unsafe content: contentId={}, userId={}", 
                    contentId, userId);
            throw new InvalidContentException(
                    "Content contains potentially malicious code. Please remove scripts, " +
                    "event handlers, and iframes before saving.");
        }
        
        content.setName(request.getName());
        content.setDescription(request.getDescription());
        content.setContent(request.getContent().trim());
        content.setCategory(request.getCategory());
        content.setUpdatedAt(LocalDateTime.now());
        
        SandboxContent updated = sandboxContentRepository.save(content);
        
        log.info("Successfully updated content: contentId={}, userId={}", contentId, userId);
        
        return SandboxContentMapper.toDto(updated);
    }
}