package hu.kornel.server.application.usecase.sandbox;

import hu.kornel.server.application.dto.sandbox.SandboxContentListDto;
import hu.kornel.server.application.dto.sandbox.SandboxContentMapper;
import hu.kornel.server.domain.entities.ContentSource;
import hu.kornel.server.domain.entities.ContentType;
import hu.kornel.server.domain.repository.SandboxContentRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ListBuiltinExamplesUseCase {
    
    private final SandboxContentRepositoryInterface sandboxContentRepository;
    
    @Transactional(readOnly = true)
    public List<SandboxContentListDto> execute(ContentType type) {
        log.debug("Listing built-in examples for type: {}", type);
        
        List<SandboxContentListDto> examples = sandboxContentRepository
                .findByTypeAndSource(type, ContentSource.BUILTIN)
                .stream()
                .map(SandboxContentMapper::toListDto)
                .collect(Collectors.toList());
        
        log.debug("Found {} built-in examples for type: {}", examples.size(), type);
        return examples;
    }
}