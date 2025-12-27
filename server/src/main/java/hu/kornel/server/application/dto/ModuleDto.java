package hu.kornel.server.application.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ModuleDto {
    private String id;
    private String title;
    private String description;
    private Integer order;

    @JsonProperty("estimated_time")
    private String estimatedTime;
    
    private List<LessonSummaryDto> lessons;
}
