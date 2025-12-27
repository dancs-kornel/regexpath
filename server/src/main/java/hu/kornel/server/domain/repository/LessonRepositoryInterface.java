package hu.kornel.server.domain.repository;

import java.util.List;
import java.util.Optional;

import hu.kornel.server.application.dto.ExerciseDto;
import hu.kornel.server.application.dto.LessonDto;
import hu.kornel.server.application.dto.ModuleDto;

public interface  LessonRepositoryInterface {
    List<ModuleDto> getAllModules();
    Optional<ModuleDto> getModuleById(String moduleId);
    Optional<LessonDto> getLessonById(String lessonId);
    List<LessonDto> getLessonsByModuleId(String moduleId);
    Optional<ExerciseDto> getExerciseById(String lessonId, String exerciseId);
    List<String> getAllModuleIds();
    List<String> getAllLessonIds();
    void reloadContent();
}
