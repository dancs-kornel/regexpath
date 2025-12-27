package hu.kornel.server.application.usecase.sandbox;

import hu.kornel.server.application.dto.sandbox.SandboxContentDto;
import hu.kornel.server.application.dto.sandbox.SandboxContentMapper;
import hu.kornel.server.application.dto.sandbox.UploadContentRequest;
import hu.kornel.server.domain.entities.ContentSource;
import hu.kornel.server.domain.entities.SandboxContent;
import hu.kornel.server.domain.exception.InvalidContentException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.SandboxContentRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.service.ContentValidationService;
import hu.kornel.server.domain.service.ContentValidationService.ValidationResult;
import hu.kornel.server.infrastructure.config.FileUploadConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class UploadContentUseCase {
    
    private final SandboxContentRepositoryInterface sandboxContentRepository;
    private final UserRepositoryInterface userRepository;
    private final ContentValidationService validationService;
    
    @Transactional
    public SandboxContentDto execute(
            MultipartFile file,
            UploadContentRequest request,
            Long userId
    ) {
        log.debug("Uploading content: type={}, name={}, userId={}", 
                request.getType(), request.getName(), userId);
        
        
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read uploaded file", e);
            throw new InvalidContentException("Failed to read uploaded file: " + e.getMessage(), e);
        }
        
        
        ValidationResult validationResult = validationService.validateContent(
                content,
                request.getType(),
                file.getOriginalFilename(),
                file.getSize()
        );
        
        if (!validationResult.isValid()) {
            log.warn("Content validation failed: {}", validationResult.errorMessage());
            throw new InvalidContentException(validationResult.errorMessage());
        }
        
        
        SandboxContent sandboxContent = SandboxContent.builder()
                .type(request.getType())
                .name(request.getName())
                .description(request.getDescription())
                .content(validationResult.sanitizedContent())
                .source(ContentSource.USER_UPLOADED)
                .ownerId(userId)
                .category(request.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        
        SandboxContent saved = sandboxContentRepository.save(sandboxContent);
        
        log.info("Successfully uploaded content: id={}, userId={}, type={}", 
                saved.getId(), userId, saved.getType());
        
        return SandboxContentMapper.toDto(saved);
    }
}