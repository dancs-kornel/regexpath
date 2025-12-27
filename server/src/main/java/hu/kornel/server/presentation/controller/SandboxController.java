package hu.kornel.server.presentation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hu.kornel.server.application.dto.sandbox.ForkContentRequest;
import hu.kornel.server.application.dto.sandbox.RegexExampleRequestDto;
import hu.kornel.server.application.dto.sandbox.RegexExampleResponseDto;
import hu.kornel.server.application.dto.sandbox.SandboxContentDto;
import hu.kornel.server.application.dto.sandbox.SandboxContentListDto;
import hu.kornel.server.application.dto.sandbox.UpdateContentRequest;
import hu.kornel.server.application.dto.sandbox.UploadContentRequest;
import hu.kornel.server.application.service.RegexExampleGeneratorApplicationService;
import hu.kornel.server.application.service.SandboxContentApplicationService;
import hu.kornel.server.domain.entities.ContentType;
import hu.kornel.server.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/sandbox")
@RequiredArgsConstructor
@Slf4j
public class SandboxController {

    private final RegexExampleGeneratorApplicationService exampleGeneratorService;
    private final SandboxContentApplicationService sandboxContentService;
    
    @PostMapping("/regex/examples")
    public ResponseEntity<RegexExampleResponseDto> generateRegexExamples(
            @Valid @RequestBody RegexExampleRequestDto request) {

        log.debug("POST /api/sandbox/regex/examples - Generating examples for pattern: {}",
                request.getPattern());

        RegexExampleResponseDto response = exampleGeneratorService.generateExamples(request);

        if (response.getErrorMessage() != null) {
            log.warn("Example generation failed: {}", response.getErrorMessage());
            return ResponseEntity.ok(response);
        }

        log.debug("Successfully generated {} positive and {} negative examples",
                response.getPositiveExamples().size(),
                response.getNegativeExamples().size());

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/examples/builtin")
    public ResponseEntity<List<SandboxContentListDto>> listBuiltinExamples(
            @RequestParam ContentType type) {
        
        log.debug("GET /api/sandbox/examples/builtin?type={}", type);
        
        List<SandboxContentListDto> examples = sandboxContentService.listBuiltinExamples(type);
        
        log.debug("Found {} built-in examples", examples.size());
        return ResponseEntity.ok(examples);
    }
    
    
    @GetMapping("/examples/user")
    public ResponseEntity<List<SandboxContentListDto>> getUserContents(
            @RequestParam ContentType type,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal.getId();
        log.debug("GET /api/sandbox/examples/user?type={} - userId: {}", type, userId);
        
        List<SandboxContentListDto> contents = sandboxContentService.getUserContents(userId, type);
        
        log.debug("Found {} user contents", contents.size());
        return ResponseEntity.ok(contents);
    }
    
    
    @GetMapping("/examples/{id}")
    public ResponseEntity<SandboxContentDto> getContentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = (userPrincipal != null) ? userPrincipal.getId() : null;
        log.debug("GET /api/sandbox/examples/{} - userId: {}", id, userId);
        
        SandboxContentDto content = sandboxContentService.getContentById(id, userId);
        
        log.debug("Retrieved content: {}", content.getName());
        return ResponseEntity.ok(content);
    }
    
    
    @PostMapping("/examples/upload")
    public ResponseEntity<SandboxContentDto> uploadContent(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") ContentType type,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal.getId();
        log.debug("POST /api/sandbox/examples/upload - userId: {}, type: {}, name: {}", 
                userId, type, name);
        
        
        UploadContentRequest request = UploadContentRequest.builder()
                .type(type)
                .name(name)
                .description(description)
                .category(category)
                .build();
        
        SandboxContentDto uploaded = sandboxContentService.uploadContent(file, request, userId);
        
        log.info("Successfully uploaded content: id={}, name={}", uploaded.getId(), uploaded.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }
    
    
    @PostMapping("/examples/{id}/fork")
    public ResponseEntity<SandboxContentDto> forkBuiltinContent(
            @PathVariable Long id,
            @Valid @RequestBody ForkContentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal.getId();
        log.debug("POST /api/sandbox/examples/{}/fork - userId: {}", id, userId);
        
        SandboxContentDto forked = sandboxContentService.forkBuiltinContent(id, request, userId);
        
        log.info("Successfully forked content: originalId={}, forkId={}", id, forked.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(forked);
    }
    
    
    @PutMapping("/examples/{id}")
    public ResponseEntity<SandboxContentDto> updateUserContent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal.getId();
        log.debug("PUT /api/sandbox/examples/{} - userId: {}", id, userId);
        
        SandboxContentDto updated = sandboxContentService.updateUserContent(id, request, userId);
        
        log.info("Successfully updated content: id={}", id);
        return ResponseEntity.ok(updated);
    }
    
    
    @DeleteMapping("/examples/{id}")
    public ResponseEntity<Void> deleteUserContent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long userId = userPrincipal.getId();
        log.debug("DELETE /api/sandbox/examples/{} - userId: {}", id, userId);
        
        sandboxContentService.deleteUserContent(id, userId);
        
        log.info("Successfully deleted content: id={}", id);
        return ResponseEntity.noContent().build();
    }
}