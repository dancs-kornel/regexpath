package hu.kornel.server.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.kornel.server.application.dto.ExerciseDto;
import hu.kornel.server.application.dto.LessonDto;
import hu.kornel.server.application.dto.ModuleDto;
import hu.kornel.server.application.dto.publicDto.PublicLessonDto;
import hu.kornel.server.application.service.DtoMapperService;
import hu.kornel.server.application.usecase.difficulty.GetUserDifficultyUseCase;
import hu.kornel.server.domain.entities.DifficultyLevel;
import hu.kornel.server.domain.repository.LessonRepositoryInterface;
import hu.kornel.server.infrastructure.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins="http://localhost:4200")
public class LessonController {

    private final LessonRepositoryInterface lessonRepository;
    private final DtoMapperService dtoMapperService;
    private final GetUserDifficultyUseCase getUserDifficultyUseCase;

    @GetMapping("/modules")
    public ResponseEntity<List<ModuleDto>> getAllModules() {
        log.debug("GET api/modules - Fetching all modules");
        List<ModuleDto> modules = lessonRepository.getAllModules();
        log.debug("Found {} modules", modules.size());
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<ModuleDto> getModuleById(@PathVariable String moduleId) {
        log.debug("GET /api/modules/{} - Fetching module details", moduleId);
        return lessonRepository.getModuleById(moduleId).map(module -> {
            log.debug("Found module: {} with {} lessons", module.getTitle(), module.getLessons() != null ? module.getLessons().size() : 0);
            return ResponseEntity.ok(module);
        }).orElseGet(() -> {
            log.warn("Module not found: {}", moduleId);
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<PublicLessonDto> getLessonById(
            @PathVariable String lessonId,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.debug("GET /api/lessons/{} - Fetching lesson details", lessonId);

        DifficultyLevel effectiveDifficulty = difficulty;
        if (effectiveDifficulty == null && userPrincipal != null) {
            try {
                effectiveDifficulty = getUserDifficultyUseCase.execute(userPrincipal.getId());
                log.debug("Using user's difficulty level: {}", effectiveDifficulty);
            } catch (Exception e) {
                log.warn("Could not get user difficulty level, using default: {}", e.getMessage());
                effectiveDifficulty = DifficultyLevel.MEDIUM;
            }
        }
        if (effectiveDifficulty == null) {
            effectiveDifficulty = DifficultyLevel.MEDIUM;
            log.debug("Using default difficulty level: MEDIUM");
        }

        final DifficultyLevel finalDifficulty = effectiveDifficulty;

        return lessonRepository.getLessonById(lessonId).map(lesson -> {
            log.debug("Found lesson: {} with {} exercises", lesson.getTitle(),
                lesson.getExercises() != null ? lesson.getExercises().size() : 0);

            
            PublicLessonDto publicLesson = dtoMapperService.toPublicLessonDto(lesson, finalDifficulty);
            log.debug("Converted to PublicLessonDto with difficulty: {}", finalDifficulty);

            return ResponseEntity.ok(publicLesson);
        }).orElseGet(() -> {
            log.warn("Lesson not found: {}", lessonId);
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping("/lessons/{lessonId}/exercises/{exerciseId}")
    public ResponseEntity<ExerciseDto> getExerciseById(@PathVariable String lessonId, @PathVariable String exerciseId) {
        log.debug("GET /api/lessons/{}/exercises/{} - Fetching exercise details", lessonId, exerciseId);
        return lessonRepository.getExerciseById(lessonId, exerciseId).map(exercise -> {
            log.debug("Found exercise: {} of type {}", exercise.getTitle(), exercise.getType());
            return ResponseEntity.ok(exercise);
        }).orElseGet(() -> {
            log.warn("Exercise not found: lessonId={}, exerciseId={}", lessonId, exerciseId);
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<List<LessonDto>> getLessonsByModuleId(@PathVariable String moduleId) {
        log.debug("GET /api/modules/{}/lessons - Fetching lessons for module", moduleId);
        List<LessonDto> lessons = lessonRepository.getLessonsByModuleId(moduleId);
        if (lessons.isEmpty()) {
            if (lessonRepository.getModuleById(moduleId).isEmpty()) {
                log.warn("Module not found: {}", moduleId);
                return ResponseEntity.notFound().build();
            }
            log.debug("Module {} exists but has no lessons", moduleId);
        }
        
        log.debug("Found {} lessons for module {}", lessons.size(), moduleId);
        return ResponseEntity.ok(lessons);
    }

    @PostMapping("/admin/reload-content")
    public ResponseEntity<String> reloadContent() {
        log.info("POST /api/admin/reload-content - Reloading lesson content");
        
        try {
            lessonRepository.reloadContent();
            return ResponseEntity.ok("Content reloaded successfully");
        } catch (Exception e) {
            log.error("Failed to reload content", e);
            return ResponseEntity.internalServerError()
                .body("Failed to reload content: " + e.getMessage());
        }
    }
    
}

