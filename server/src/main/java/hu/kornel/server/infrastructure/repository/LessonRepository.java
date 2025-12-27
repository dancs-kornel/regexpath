package hu.kornel.server.infrastructure.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import hu.kornel.server.application.dto.ExerciseDto;
import hu.kornel.server.application.dto.LessonDto;
import hu.kornel.server.application.dto.ModuleDto;
import hu.kornel.server.domain.repository.LessonRepositoryInterface;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class LessonRepository implements LessonRepositoryInterface {
    private final ObjectMapper yamlMapper;
    private final ResourcePatternResolver resourceResolver;

    @Value("${lessons.path:classpath:lessons/}")
    private String lessonsBasePath;

    private final List<ModuleDto> modules = new ArrayList<>();
    private final Map<String, ModuleDto> moduleCache = new HashMap<>();
    private final Map<String, LessonDto> lessonCache = new HashMap<>();

    public LessonRepository() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
        this.resourceResolver = new PathMatchingResourcePatternResolver();
    }

    @PostConstruct
    public void loadAllContent() {
        log.info("Loading lesson content from: {}", lessonsBasePath);
        try {
            loadModules();
            loadLessons();
            log.info("Successfully loaded {} modules with {} lessons",
                    modules.size(), lessonCache.size());
        } catch (Exception e) {
            log.error("Failed to load lesson content", e);
            throw new RuntimeException("Failed to initialize lesson repository", e);
        }

    }

    private void loadModules() {
        try {
            loadModulesIndex();
            loadIndividualModules();
            sortModulesByOrder();
        } catch (IOException e) {
            log.error("Failed to load modules", e);
            throw new RuntimeException("Failed to initialize lesson repository", e);
        }
    }

    private void loadModulesIndex() throws IOException {
        Resource modulesResource = resourceResolver.getResource(lessonsBasePath + "modules.yaml");
        if (!modulesResource.exists())
            return;
        ModulesIndex modulesIndex = yamlMapper.readValue(modulesResource.getInputStream(), ModulesIndex.class);
        modules.addAll(modulesIndex.getModules());
    }

    private void loadIndividualModules() throws IOException {
        Resource[] moduleResources = resourceResolver.getResources(lessonsBasePath + "*/module.yaml");
        for (Resource moduleResource : moduleResources) {
            loadSingleModule(moduleResource);
        }
    }

    private void loadSingleModule(Resource moduleResource) {
        try {
            ModuleWrapper wrapper = yamlMapper.readValue(moduleResource.getInputStream(), ModuleWrapper.class);
            ModuleDto module = wrapper.getModule();

            ModuleDto existingModule = findModuleById(module.getId());
            if (existingModule != null)
                existingModule.setLessons(module.getLessons());
            else
                modules.add(module);

            moduleCache.put(module.getId(), module);
            log.debug("Loaded module: {}", module.getId());
        } catch (IOException e) {
            log.warn("Failed to load module from: {}", moduleResource.getFilename(), e);
        }
    }

    private void sortModulesByOrder() {
        modules.sort(Comparator.comparing(ModuleDto::getOrder, Comparator.nullsLast(Integer::compareTo)));
    }

    private void loadLessons() {
        try {
            Resource[] lessonResources = resourceResolver.getResources(lessonsBasePath + "*/*.yaml");

            for (Resource lessonResource : lessonResources) {
                if ("module.yaml".equals(lessonResource.getFilename()))
                    continue;
                loadSingleLesson(lessonResource);
            }
        } catch (IOException e) {
            log.error("Failed to load lessons", e);
            throw new RuntimeException("Failed to initialize lesson repository", e);
        }
    }

    private void loadSingleLesson(Resource lessonResource) {
        try {
            LessonWrapper wrapper = yamlMapper.readValue(lessonResource.getInputStream(), LessonWrapper.class);
            LessonDto lesson = wrapper.getLesson();
            sortExercisesByOrder(lesson);
            if (lesson.getIntroduction() != null) {
                lesson.setIntroduction(formatIntroduction(lesson.getIntroduction()));
            }
            lessonCache.put(lesson.getId(), lesson);
            log.debug("Loaded lesson: {} with {} exercises", lesson.getId(),
                    lesson.getExercises() != null ? lesson.getExercises().size() : 0);

        } catch (IOException e) {
            log.warn("Failed to load lesson from: {}", lessonResource.getFilename(), e);
        }
    }

    private void sortExercisesByOrder(LessonDto lesson) {
        if (lesson.getExercises() != null)
            lesson.getExercises()
                    .sort(Comparator.comparing(ExerciseDto::getOrder, Comparator.nullsLast(Integer::compareTo)));
    }

    @Override
    public List<ModuleDto> getAllModules() {
        return new ArrayList<>(modules);
    }

    @Override
    public Optional<ModuleDto> getModuleById(String moduleId) {
        return Optional.ofNullable(moduleCache.get(moduleId));
    }

    @Override
    public Optional<LessonDto> getLessonById(String lessonId) {
        return Optional.ofNullable(lessonCache.get(lessonId));
    }

    @Override
    public List<LessonDto> getLessonsByModuleId(String moduleId) {
        ModuleDto module = moduleCache.get(moduleId);
        if (module == null || module.getLessons() == null) {
            return Collections.emptyList();
        }

        return module.getLessons().stream()
                .map(summary -> lessonCache.get(summary.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ExerciseDto> getExerciseById(String lessonId, String exerciseId) {
        LessonDto lesson = lessonCache.get(lessonId);
        if (lesson == null || lesson.getExercises() == null) {
            return Optional.empty();
        }

        return lesson.getExercises().stream()
                .filter(exercise -> exerciseId.equals(exercise.getId()))
                .findFirst();
    }

    @Override
    public List<String> getAllModuleIds() {
        return modules.stream()
                .map(ModuleDto::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllLessonIds() {
        return new ArrayList<>(lessonCache.keySet());
    }

    @Override
    public void reloadContent() {
        log.info("Reloading lesson content...");
        modules.clear();
        moduleCache.clear();
        lessonCache.clear();
        loadAllContent();
    }

    private ModuleDto findModuleById(String moduleId) {
        return modules.stream()
                .filter(module -> moduleId.equals(module.getId()))
                .findFirst()
                .orElse(null);
    }

    @Data
    private static class ModulesIndex {
        private List<ModuleDto> modules;
    }

    @Data
    private static class ModuleWrapper {
        private ModuleDto module;
    }

    @Data
    private static class LessonWrapper {
        private LessonDto lesson;
    }

    private String formatIntroduction(String raw) {
        if (raw == null) {
            return null;
        }

        
        String safe = raw
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        
        safe = safe.replaceAll("(?s)`([^`]+)`", "<code>$1</code>");
        safe = safe.replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>");
        safe = safe.replace("\n", "<br/>");

        return safe;
    }

}
