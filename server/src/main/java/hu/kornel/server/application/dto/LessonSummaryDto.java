package hu.kornel.server.application.dto;

import java.util.Collections;
import java.util.List;

import lombok.Data;

@Data
public class LessonSummaryDto {
    private String id;
    private String title;
    private String description;
    private Integer order;
    private List<String> prerequisites;

    public List<String> getPrerequisites() {
        return prerequisites != null ? prerequisites : Collections.emptyList();
    }

    public boolean hasPrerequisites() {
        return prerequisites != null && !prerequisites.isEmpty();
    }
}
