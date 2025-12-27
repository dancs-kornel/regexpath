package hu.kornel.server.application.usecase.lessons;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import hu.kornel.server.application.dto.LessonSummaryDto;
import hu.kornel.server.application.dto.ModuleDto;
import hu.kornel.server.application.dto.lessons.PrerequisiteCheckResponse;
import hu.kornel.server.domain.exception.LessonNotFoundException;
import hu.kornel.server.domain.repository.LessonProgressRepositoryInterface;
import hu.kornel.server.domain.repository.LessonRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckLessonPrerequisitesUseCase {
    private final LessonRepositoryInterface lessonRepository;
    private final LessonProgressRepositoryInterface lessonProgressRepository;

    
    public PrerequisiteCheckResponse execute(String lessonId, Long userId) {
        
        LessonSummaryDto lessonSummary = findLessonSummary(lessonId);
        
        if (lessonSummary == null) {
            throw new LessonNotFoundException("Lesson not found: " + lessonId);
        }

        
        if (!lessonSummary.hasPrerequisites()) {
            log.debug("Lesson {} has no prerequisites, access granted", lessonId);
            return PrerequisiteCheckResponse.builder()
                    .canAccess(true)
                    .unmetPrerequisites(new ArrayList<>())
                    .build();
        }

        
        if (userId == null) {
            log.debug("Guest user accessing lesson {} with prerequisites", lessonId);
            List<PrerequisiteCheckResponse.PrerequisiteInfo> prerequisiteInfos = 
                    buildPrerequisiteInfoList(lessonSummary.getPrerequisites(), null);
            
            return PrerequisiteCheckResponse.builder()
                    .canAccess(true)  
                    .unmetPrerequisites(prerequisiteInfos)
                    .build();
        }

        
        List<PrerequisiteCheckResponse.PrerequisiteInfo> unmetPrerequisites = 
                lessonSummary.getPrerequisites().stream()
                        .filter(prereqId -> !isPrerequisiteCompleted(prereqId, userId))
                        .map(this::buildPrerequisiteInfo)
                        .collect(Collectors.toList());

        boolean canAccess = unmetPrerequisites.isEmpty();

        log.debug("User {} {} access lesson {}. Unmet prerequisites: {}", 
                userId, canAccess ? "CAN" : "CANNOT", lessonId, unmetPrerequisites.size());

        return PrerequisiteCheckResponse.builder()
                .canAccess(canAccess)
                .unmetPrerequisites(unmetPrerequisites)
                .build();
    }

    
    private LessonSummaryDto findLessonSummary(String lessonId) {
        List<ModuleDto> modules = lessonRepository.getAllModules();
        
        for (ModuleDto module : modules) {
            if (module.getLessons() != null) {
                for (LessonSummaryDto lesson : module.getLessons()) {
                    if (lessonId.equals(lesson.getId())) {
                        return lesson;
                    }
                }
            }
        }
        
        return null;
    }

    private boolean isPrerequisiteCompleted(String lessonId, Long userId) {
        var progressOpt = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId);
        
        if (progressOpt.isEmpty()) {
            return false;
        }
        
        return progressOpt.get().isCompleted();
    }

    private PrerequisiteCheckResponse.PrerequisiteInfo buildPrerequisiteInfo(String lessonId) {
        LessonSummaryDto lessonSummary = findLessonSummary(lessonId);
        
        if (lessonSummary != null) {
            return PrerequisiteCheckResponse.PrerequisiteInfo.builder()
                    .lessonId(lessonSummary.getId())
                    .title(lessonSummary.getTitle())
                    .description(lessonSummary.getDescription())
                    .completed(false)
                    .build();
        } else {
            log.warn("Prerequisite lesson not found: {}", lessonId);
            return PrerequisiteCheckResponse.PrerequisiteInfo.builder()
                    .lessonId(lessonId)
                    .title("Unknown Lesson")
                    .description("Prerequisite lesson not found")
                    .completed(false)
                    .build();
        }
    }

    private List<PrerequisiteCheckResponse.PrerequisiteInfo> buildPrerequisiteInfoList(
            List<String> prerequisiteIds, Long userId) {
        return prerequisiteIds.stream()
                .map(prereqId -> {
                    boolean completed = userId != null && isPrerequisiteCompleted(prereqId, userId);
                    LessonSummaryDto lessonSummary = findLessonSummary(prereqId);
                    
                    if (lessonSummary != null) {
                        return PrerequisiteCheckResponse.PrerequisiteInfo.builder()
                                .lessonId(lessonSummary.getId())
                                .title(lessonSummary.getTitle())
                                .description(lessonSummary.getDescription())
                                .completed(completed)
                                .build();
                    } else {
                        return PrerequisiteCheckResponse.PrerequisiteInfo.builder()
                                .lessonId(prereqId)
                                .title("Unknown Lesson")
                                .description("Prerequisite lesson not found")
                                .completed(false)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }
}