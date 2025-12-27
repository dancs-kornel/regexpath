package hu.kornel.server.application.usecase.sandbox;

import hu.kornel.server.application.dto.sandbox.SandboxContentListDto;
import hu.kornel.server.application.dto.sandbox.SandboxContentMapper;
import hu.kornel.server.domain.entities.ContentType;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.SandboxContentRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class GetUserContentsUseCase {
    
    private final SandboxContentRepositoryInterface sandboxContentRepository;
    private final UserRepositoryInterface userRepository;
    
    @Transactional(readOnly = true)
    public List<SandboxContentListDto> execute(Long userId, ContentType type) {
        log.debug("Getting user contents for userId: {} and type: {}", userId, type);
        
        
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        
        List<SandboxContentListDto> contents = sandboxContentRepository
                .findByOwnerIdAndType(userId, type)
                .stream()
                .map(SandboxContentMapper::toListDto)
                .collect(Collectors.toList());
        
        log.debug("Found {} user contents for userId: {} and type: {}", 
                contents.size(), userId, type);
        return contents;
    }
}