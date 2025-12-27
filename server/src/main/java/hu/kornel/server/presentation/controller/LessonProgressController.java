package hu.kornel.server.presentation.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.kornel.server.application.dto.lessons.ContinueLearningResponse;
import hu.kornel.server.application.dto.lessons.LessonProgressResponse;
import hu.kornel.server.application.dto.lessons.ModuleProgressResponse;
import hu.kornel.server.application.dto.lessons.PrerequisiteCheckResponse;
import hu.kornel.server.application.usecase.lessons.CheckLessonPrerequisitesUseCase;
import hu.kornel.server.application.usecase.lessons.GetContinueLearningUseCase;
import hu.kornel.server.application.usecase.lessons.GetModuleProgressUseCase;
import hu.kornel.server.application.usecase.lessons.GetUserLessonProgressUseCase;
import hu.kornel.server.application.usecase.lessons.MarkLessonCompleteUseCase;
import hu.kornel.server.application.usecase.lessons.RecordLessonAccessUseCase;
import hu.kornel.server.infrastructure.security.UserPrincipal;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonProgressController {

    private final GetUserLessonProgressUseCase getUserLessonProgressUseCase;
    private final GetModuleProgressUseCase getModuleProgressUseCase;
    private final MarkLessonCompleteUseCase markLessonCompleteUseCase;
    private final RecordLessonAccessUseCase recordLessonAccessUseCase;
    private final GetContinueLearningUseCase getContinueLearningUseCase;
    private final CheckLessonPrerequisitesUseCase checkLessonPrerequisitesUseCase;

    
    @GetMapping("/progress")
    public ResponseEntity<List<LessonProgressResponse>> getUserProgress(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<LessonProgressResponse> progress = getUserLessonProgressUseCase.execute(userPrincipal.getId());
        return ResponseEntity.ok(progress);
    }

    
    @GetMapping("/progress/modules")
    public ResponseEntity<List<ModuleProgressResponse>> getModuleProgress(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ModuleProgressResponse> progress = getModuleProgressUseCase.execute(userPrincipal.getId());
        return ResponseEntity.ok(progress);
    }

    
    @PostMapping("/{lessonId}/complete")
    public ResponseEntity<LessonProgressResponse> markLessonComplete(
            @PathVariable String lessonId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        LessonProgressResponse response = markLessonCompleteUseCase.execute(lessonId, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    
    @PostMapping("/{lessonId}/access")
    public ResponseEntity<Void> recordLessonAccess(
            @PathVariable String lessonId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        recordLessonAccessUseCase.execute(lessonId, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    
    @GetMapping("/progress/continue")
    public ResponseEntity<ContinueLearningResponse> getContinueLearning(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Optional<ContinueLearningResponse> response = getContinueLearningUseCase.execute(userPrincipal.getId());
        return response
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{lessonId}/prerequisites/check")
    public ResponseEntity<PrerequisiteCheckResponse> checkPrerequisites(
            @PathVariable String lessonId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        
        Long userId = (userPrincipal != null) ? userPrincipal.getId() : null;
        PrerequisiteCheckResponse response = checkLessonPrerequisitesUseCase.execute(lessonId, userId);

        return ResponseEntity.ok(response);
    }
}