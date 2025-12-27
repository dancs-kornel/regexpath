package hu.kornel.server.application.service;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hu.kornel.server.application.dto.ExerciseDto;
import hu.kornel.server.application.dto.MultipleChoiceExerciseDto;
import hu.kornel.server.application.dto.RadioExerciseDto;
import hu.kornel.server.application.dto.RegexSandboxExerciseDto;
import hu.kornel.server.application.dto.XPathSandboxExerciseDto;
import hu.kornel.server.domain.entities.assignments.Exercise;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExerciseDtoConverter {
    
    private final ObjectMapper objectMapper;
    
    
    public ExerciseDto toDto(Exercise exercise) {
        try {
            JsonNode config = objectMapper.readTree(exercise.getConfigJson());
            
            return switch (exercise.getType()) {
                case MULTIPLE_CHOICE -> convertToMultipleChoice(exercise, config);
                case RADIO -> convertToRadio(exercise, config);
                case REGEX_SANDBOX -> convertToRegexSandbox(exercise, config);
                case XPATH_SANDBOX -> convertToXPathSandbox(exercise, config);
            };
        } catch (Exception e) {
            log.error("Failed to convert exercise {} to DTO", exercise.getId(), e);
            throw new RuntimeException("Failed to parse exercise configuration", e);
        }
    }
    
    private MultipleChoiceExerciseDto convertToMultipleChoice(Exercise exercise, JsonNode config) {
        MultipleChoiceExerciseDto dto = new MultipleChoiceExerciseDto();
        dto.setId(exercise.getId().toString());
        dto.setTitle(exercise.getTitle());
        dto.setQuestion(exercise.getQuestion());
        dto.setExplanation(exercise.getExplanation());
        dto.setOptions(objectMapper.convertValue(config.get("options"), 
                objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, 
                hu.kornel.server.application.dto.OptionDto.class)));
        return dto;
    }
    
    private RadioExerciseDto convertToRadio(Exercise exercise, JsonNode config) {
        RadioExerciseDto dto = new RadioExerciseDto();
        dto.setId(exercise.getId().toString());
        dto.setTitle(exercise.getTitle());
        dto.setQuestion(exercise.getQuestion());
        dto.setExplanation(exercise.getExplanation());
        dto.setOptions(objectMapper.convertValue(config.get("options"), 
                objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, 
                hu.kornel.server.application.dto.OptionDto.class)));
        return dto;
    }
    
    private RegexSandboxExerciseDto convertToRegexSandbox(Exercise exercise, JsonNode config) {
        RegexSandboxExerciseDto dto = new RegexSandboxExerciseDto();
        dto.setId(exercise.getId().toString());
        dto.setTitle(exercise.getTitle());
        dto.setQuestion(exercise.getQuestion());
        dto.setExplanation(exercise.getExplanation());
        dto.setSolution(config.has("solution") ? config.get("solution").asText() : null);
        dto.setTestCases(objectMapper.convertValue(config.get("testCases"), 
                hu.kornel.server.application.dto.RegexTestCasesDto.class));
        
        boolean enableHighlighting = true; 
        if (config.has("enableRealTimeHighlighting")) {
            enableHighlighting = config.get("enableRealTimeHighlighting").asBoolean(true);
        }
        dto.setEnableRealTimeHighlighting(enableHighlighting);
        
        return dto;
    }
    
    private XPathSandboxExerciseDto convertToXPathSandbox(Exercise exercise, JsonNode config) {
        XPathSandboxExerciseDto dto = new XPathSandboxExerciseDto();
        dto.setId(exercise.getId().toString());
        dto.setTitle(exercise.getTitle());
        dto.setQuestion(exercise.getQuestion());
        dto.setExplanation(exercise.getExplanation());
        dto.setSampleDocument(config.has("sampleDocument") ? config.get("sampleDocument").asText() : null);
        dto.setSolution(config.has("solution") ? config.get("solution").asText() : null);
        
        boolean enableHighlighting = true; 
        if (config.has("enableRealTimeHighlighting")) {
            enableHighlighting = config.get("enableRealTimeHighlighting").asBoolean(true);
        }
        dto.setEnableRealTimeHighlighting(enableHighlighting);
        
        return dto;
    }
}