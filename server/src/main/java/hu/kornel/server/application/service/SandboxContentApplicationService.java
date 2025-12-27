package hu.kornel.server.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import hu.kornel.server.application.dto.sandbox.ForkContentRequest;
import hu.kornel.server.application.dto.sandbox.SandboxContentDto;
import hu.kornel.server.application.dto.sandbox.SandboxContentListDto;
import hu.kornel.server.application.dto.sandbox.UpdateContentRequest;
import hu.kornel.server.application.dto.sandbox.UploadContentRequest;
import hu.kornel.server.application.usecase.sandbox.DeleteUserContentUseCase;
import hu.kornel.server.application.usecase.sandbox.ForkBuiltinContentUseCase;
import hu.kornel.server.application.usecase.sandbox.GetContentByIdUseCase;
import hu.kornel.server.application.usecase.sandbox.GetUserContentsUseCase;
import hu.kornel.server.application.usecase.sandbox.ListBuiltinExamplesUseCase;
import hu.kornel.server.application.usecase.sandbox.UpdateUserContentUseCase;
import hu.kornel.server.application.usecase.sandbox.UploadContentUseCase;
import hu.kornel.server.domain.entities.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class SandboxContentApplicationService {
    
    private final ListBuiltinExamplesUseCase listBuiltinExamplesUseCase;
    private final GetUserContentsUseCase getUserContentsUseCase;
    private final GetContentByIdUseCase getContentByIdUseCase;
    private final UploadContentUseCase uploadContentUseCase;
    private final ForkBuiltinContentUseCase forkBuiltinContentUseCase;
    private final UpdateUserContentUseCase updateUserContentUseCase;
    private final DeleteUserContentUseCase deleteUserContentUseCase;
    
    
    public List<SandboxContentListDto> listBuiltinExamples(ContentType type) {
        log.debug("Application service: listing built-in examples for type: {}", type);
        return listBuiltinExamplesUseCase.execute(type);
    }
    
    
    public List<SandboxContentListDto> getUserContents(Long userId, ContentType type) {
        log.debug("Application service: getting user contents for userId: {} and type: {}", 
                userId, type);
        return getUserContentsUseCase.execute(userId, type);
    }
    
    
    public SandboxContentDto getContentById(Long contentId, Long userId) {
        log.debug("Application service: getting content by id: {} for user: {}", contentId, userId);
        return getContentByIdUseCase.execute(contentId, userId);
    }
    
    
    public SandboxContentDto uploadContent(MultipartFile file, UploadContentRequest request, Long userId) {
        log.debug("Application service: uploading content for userId: {}", userId);
        return uploadContentUseCase.execute(file, request, userId);
    }
    
    
    public SandboxContentDto forkBuiltinContent(Long builtinContentId, ForkContentRequest request, Long userId) {
        log.debug("Application service: forking content {} for userId: {}", 
                builtinContentId, userId);
        return forkBuiltinContentUseCase.execute(builtinContentId, request, userId);
    }
    
    
    public SandboxContentDto updateUserContent(Long contentId, UpdateContentRequest request, Long userId) {
        log.debug("Application service: updating content {} for userId: {}", contentId, userId);
        return updateUserContentUseCase.execute(contentId, request, userId);
    }
    
    
    public void deleteUserContent(Long contentId, Long userId) {
        log.debug("Application service: deleting content {} for userId: {}", contentId, userId);
        deleteUserContentUseCase.execute(contentId, userId);
    }
}