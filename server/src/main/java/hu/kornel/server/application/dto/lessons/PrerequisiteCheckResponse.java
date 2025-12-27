package hu.kornel.server.application.dto.lessons;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrerequisiteCheckResponse {
    private boolean canAccess;
    private List<PrerequisiteInfo> unmetPrerequisites;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrerequisiteInfo {
        private String lessonId;
        private String title;
        private String description;
        private boolean completed;
    }

    public boolean allPrerequisitesMet() {
        return unmetPrerequisites == null || unmetPrerequisites.isEmpty();
    }
}
