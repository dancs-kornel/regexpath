package hu.kornel.server.infrastructure.service.regex;

import hu.kornel.server.domain.valueObjects.regex.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;


@Slf4j
public class PositiveExampleGenerator implements RegexNodeVisitor<List<String>> {
    
    private final Pattern compiledPattern;
    private final int maxExamples;
    private final Set<String> generated = new HashSet<>();
    
    public PositiveExampleGenerator(Pattern compiledPattern, int maxExamples) {
        this.compiledPattern = compiledPattern;
        this.maxExamples = maxExamples;
    }
    
    public List<String> generate(RegexNode root) {
        log.debug("Generating positive examples from AST");
        List<String> results = root.accept(this);
        
        
        List<String> validated = results.stream()
            .distinct()
            .filter(s -> compiledPattern.matcher(s).matches())
            .limit(maxExamples)
            .toList();
        
        log.debug("Generated {} positive examples", validated.size());
        return validated;
    }
    
    @Override
    public List<String> visitLiteral(LiteralNode node) {
        return List.of(node.value());
    }
    
    @Override
    public List<String> visitCharClass(CharClassNode node) {
        List<Character> samples = node.sampleCharacters(3);
        return samples.stream()
            .map(String::valueOf)
            .toList();
    }
    
    @Override
    public List<String> visitConcat(ConcatNode node) {
        if (node.children().isEmpty()) {
            return List.of("");
        }
        
        
        List<String> result = new ArrayList<>(node.children().get(0).accept(this));
        
        
        for (int i = 1; i < node.children().size(); i++) {
            List<String> childExamples = node.children().get(i).accept(this);
            result = crossProduct(result, childExamples);
            
            
            if (result.size() > maxExamples * 2) {
                result = result.subList(0, maxExamples * 2);
            }
        }
        
        return result;
    }
    
    @Override
    public List<String> visitAlternation(AlternationNode node) {
        List<String> result = new ArrayList<>();
        
        for (RegexNode alternative : node.alternatives()) {
            List<String> altExamples = alternative.accept(this);
            result.addAll(altExamples);
            
            if (result.size() >= maxExamples) {
                break;
            }
        }
        
        return result;
    }
    
    @Override
    public List<String> visitQuantifier(QuantifierNode node) {
        List<String> childExamples = node.child().accept(this);
        List<String> result = new ArrayList<>();
        
        int min = node.min();
        Integer max = node.max();
        
        
        int maxReps = max != null ? Math.min(max, 3) : Math.min(min + 2, 3);
        
        for (int reps = min; reps <= maxReps; reps++) {
            if (reps == 0) {
                result.add("");
            } else {
                List<String> repeated = repeatExamples(childExamples, reps);
                result.addAll(repeated);
            }
            
            if (result.size() >= maxExamples) {
                break;
            }
        }
        
        return result;
    }
    
    @Override
    public List<String> visitGroup(GroupNode node) {
        
        return node.child().accept(this);
    }
    
    @Override
    public List<String> visitAnchor(AnchorNode node) {
        
        return List.of("");
    }
    
    @Override
    public List<String> visitDot(DotNode node) {
        
        return List.of("a", "x", "1");
    }
    
    
    
    private List<String> crossProduct(List<String> left, List<String> right) {
        List<String> result = new ArrayList<>();
        
        for (String l : left) {
            for (String r : right) {
                result.add(l + r);
                if (result.size() >= maxExamples * 2) {
                    return result;
                }
            }
        }
        
        return result;
    }
    
    private List<String> repeatExamples(List<String> examples, int times) {
        if (times == 0) {
            return List.of("");
        }
        
        if (times == 1) {
            return examples;
        }
        
        List<String> result = new ArrayList<>(examples);
        
        for (int i = 1; i < times; i++) {
            result = crossProduct(result, examples);
            
            if (result.size() > maxExamples) {
                result = result.subList(0, maxExamples);
                break;
            }
        }
        
        return result;
    }
}