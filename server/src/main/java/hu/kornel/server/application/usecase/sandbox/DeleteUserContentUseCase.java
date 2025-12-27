package hu.kornel.server.application.usecase.sandbox;

import hu.kornel.server.domain.entities.SandboxContent;
import hu.kornel.server.domain.exception.SandboxContentNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.SandboxContentRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteUserContentUseCase {
    
    private final SandboxContentRepositoryInterface sandboxContentRepository;
    private final UserRepositoryInterface userRepository;
    
    @Transactional
    public void execute(Long contentId, Long userId) {
        log.debug("Deleting content: contentId={}, userId={}", contentId, userId);
        
        
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        
        SandboxContent content = sandboxContentRepository.findById(contentId)
                .orElseThrow(() -> new SandboxContentNotFoundException(contentId));
        
        
        if (!content.canBeDeletedBy(userId)) {
            log.warn("Unauthorized delete attempt: userId {} tried to delete content {} owned by {}", 
                    userId, contentId, content.getOwnerId());
            
            if (content.isBuiltIn()) {
                throw new UnauthorizedAccessException(
                        "Cannot delete built-in content");
            } else {
                throw new UnauthorizedAccessException(
                        "You do not have permission to delete this content");
            }
        }
        
        
        sandboxContentRepository.deleteById(contentId);
        
        log.info("Successfully deleted content: contentId={}, userId={}", contentId, userId);
    }
}