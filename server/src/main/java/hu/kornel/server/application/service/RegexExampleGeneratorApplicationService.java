package hu.kornel.server.application.service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import hu.kornel.server.application.dto.sandbox.RegexExampleDto;
import hu.kornel.server.application.dto.sandbox.RegexExampleRequestDto;
import hu.kornel.server.application.dto.sandbox.RegexExampleResponseDto;
import hu.kornel.server.domain.service.RegexExampleGeneratorServiceInterface;
import hu.kornel.server.domain.service.RegexExampleGeneratorServiceInterface.NegativeExample;
import hu.kornel.server.domain.service.RegexParserServiceInterface;
import hu.kornel.server.domain.valueObjects.regex.RegexNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegexExampleGeneratorApplicationService {
    
    private final RegexParserServiceInterface parserService;
    private final RegexExampleGeneratorServiceInterface exampleGeneratorService;
    
    private static final int DEFAULT_MAX_POSITIVE = 5;
    private static final int DEFAULT_MAX_NEGATIVE = 3;

    public RegexExampleResponseDto generateExamples(RegexExampleRequestDto request) {
        String pattern = request.getPattern();
        log.info("Generating examples for pattern: {}", pattern);
        
        try {
            int flags = 0;
            if (request.isCaseInsensitive()) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            if (request.isMultiline()) {
                flags |= Pattern.MULTILINE;
            }
            Pattern compiledPattern = Pattern.compile(pattern, flags);
            
            RegexNode ast = parserService.parse(pattern);
            
            List<String> positiveStrings = exampleGeneratorService.generatePositiveExamples(ast, compiledPattern, DEFAULT_MAX_POSITIVE);
            
            List<RegexExampleDto> positiveExamples = positiveStrings.stream()
                .map(RegexExampleDto::new)
                .collect(Collectors.toList());
            
            List<NegativeExample> negativeResults = exampleGeneratorService.generateNegativeExamples(ast, compiledPattern, DEFAULT_MAX_NEGATIVE);
            
            List<RegexExampleDto> negativeExamples = negativeResults.stream()
                .map(ne -> new RegexExampleDto(ne.text(), ne.reason()))
                .collect(Collectors.toList());
            
            log.info("Successfully generated {} positive and {} negative examples", 
                positiveExamples.size(), negativeExamples.size());
            
            return RegexExampleResponseDto.success(positiveExamples, negativeExamples);
            
        } catch (PatternSyntaxException e) {
            log.warn("Invalid regex pattern: {}", e.getMessage());
            return RegexExampleResponseDto.error("Invalid regex pattern: " + e.getDescription());
            
        } catch (IllegalArgumentException e) {
            log.warn("Unsupported regex construct: {}", e.getMessage());
            return RegexExampleResponseDto.error(e.getMessage());
            
        } catch (Exception e) {
            log.error("Unexpected error generating examples", e);
            return RegexExampleResponseDto.error("Unexpected error: " + e.getMessage());
        }
    }
}