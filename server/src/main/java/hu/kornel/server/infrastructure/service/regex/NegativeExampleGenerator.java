package hu.kornel.server.infrastructure.service.regex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import hu.kornel.server.domain.service.RegexExampleGeneratorServiceInterface.NegativeExample;
import hu.kornel.server.domain.valueObjects.regex.AlternationNode;
import hu.kornel.server.domain.valueObjects.regex.AnchorNode;
import hu.kornel.server.domain.valueObjects.regex.CharClassNode;
import hu.kornel.server.domain.valueObjects.regex.ConcatNode;
import hu.kornel.server.domain.valueObjects.regex.DotNode;
import hu.kornel.server.domain.valueObjects.regex.GroupNode;
import hu.kornel.server.domain.valueObjects.regex.LiteralNode;
import hu.kornel.server.domain.valueObjects.regex.QuantifierNode;
import hu.kornel.server.domain.valueObjects.regex.RegexNode;
import hu.kornel.server.domain.valueObjects.regex.RegexNodeVisitor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class NegativeExampleGenerator {
    
    private final RegexNode root;
    private final Pattern compiledPattern;
    private final int maxExamples;
    
    public NegativeExampleGenerator(RegexNode root, Pattern compiledPattern, int maxExamples) {
        this.root = root;
        this.compiledPattern = compiledPattern;
        this.maxExamples = maxExamples;
    }
    
    
    public List<NegativeExample> generateNegativeExamples() {
        log.debug("Generating negative examples via structural mutations");
        
        Set<String> seen = new HashSet<>();
        List<NegativeExample> results = new ArrayList<>();
        
        
        PatternStructure structure = analyzeStructure();
        log.debug("Pattern structure: {}", structure);
        
        
        List<String> witnesses = generateWitnesses();
        log.debug("Generated {} witnesses: {}", witnesses.size(), witnesses);
        
        
        for (String witness : witnesses) {
            if (!mutateWitness(witness, structure, seen, results)) break;
        }
        
        
        if (structure.hasAlternation) {
            addAlternationMix(results, seen, witnesses);
        }
        
        
        if (structure.hasStartAnchor) {
            String witness = witnesses.isEmpty() ? sampleUnit(root) : witnesses.get(0);
            tryAdd(seen, results, "X" + witness, "Violated ^ anchor by prefixing");
        }
        
        if (structure.hasEndAnchor) {
            String witness = witnesses.isEmpty() ? sampleUnit(root) : witnesses.get(0);
            tryAdd(seen, results, witness + "X", "Violated $ anchor by appending");
        }
        
        
        if (results.isEmpty()) {
            generateFallbackNegatives(seen, results);
        }
        
        log.debug("Generated {} negative examples", results.size());
        return results;
    }
    
    
    private boolean tryAdd(Set<String> seen, List<NegativeExample> out, String s, String reason) {
        if (out.size() >= maxExamples) return false;
        if (!seen.add(s)) return true; 
        if (!compiledPattern.matcher(s).matches()) {
            out.add(new NegativeExample(s, reason));
        }
        return out.size() < maxExamples;
    }
    
    
    private PatternStructure analyzeStructure() {
        return root.accept(new StructureAnalyzer());
    }
    
    
    private List<String> generateWitnesses() {
        PositiveExampleGenerator generator = new PositiveExampleGenerator(compiledPattern, 5);
        List<String> positives = generator.generate(root);
        
        
        positives = positives.stream()
            .filter(s -> !s.isEmpty())
            .distinct()
            .sorted(Comparator.comparingInt(String::length))
            .toList();
        
        return positives.subList(0, Math.min(3, positives.size()));
    }
    
    
    private String sampleUnit(RegexNode node) {
        
        List<Character> samples = List.of('a', 'x', '0');
        for (char c : samples) {
            String s = String.valueOf(c);
            if (compiledPattern.matcher(s).find()) {
                return s;
            }
        }
        return "a"; 
    }
    
    
    private boolean mutateWitness(String witness, PatternStructure structure, Set<String> seen, List<NegativeExample> out) {
        
        
        if (structure.hasQuantifiers && witness.length() > 0) {
            String shortened = witness.substring(0, Math.max(0, witness.length() - 1));
            if (!tryAdd(seen, out, shortened, "One less than minimum repetitions")) {
                return false;
            }
        }
        
        
        if (structure.hasQuantifiers && witness.length() > 0) {
            char lastChar = witness.charAt(witness.length() - 1);
            String lengthened = witness + lastChar;
            if (!tryAdd(seen, out, lengthened, "One more than maximum repetitions")) {
                return false;
            }
        }
        
        
        if ((structure.hasCharClasses || structure.hasLiterals) && witness.length() > 0) {
            int mid = witness.length() / 2;
            char original = witness.charAt(mid);
            
            
            char violation;
            if (structure.hasDigits && Character.isDigit(original)) {
                violation = 'x'; 
            } else if (structure.hasLetters && Character.isLetter(original)) {
                violation = '9'; 
            } else if (structure.hasWordChars) {
                violation = '@'; 
            } else {
                violation = original == 'a' ? 'b' : 'a';
            }
            
            String mutated = witness.substring(0, mid) + violation + witness.substring(mid + 1);
            if (!tryAdd(seen, out, mutated, "Changed character to violate pattern")) {
                return false;
            }
        }
        
        
        if (structure.hasLiterals && witness.length() > 1) {
            String deleted = witness.substring(0, witness.length() - 1);
            if (!tryAdd(seen, out, deleted, "Deleted required character")) {
                return false;
            }
        }
        
        
        if (witness.length() > 0) {
            if (!tryAdd(seen, out, witness + "!", "Added unexpected character")) {
                return false;
            }
        }
        
        return true; 
    }
    
    
    private void addAlternationMix(List<NegativeExample> out, Set<String> seen, List<String> witnesses) {
        if (witnesses.size() < 2) return;
        
        String a = witnesses.get(0);
        String b = witnesses.get(1);
        
        if (a.equals(b) || a.isEmpty() || b.isEmpty()) return;
        
        String mixed = a.substring(0, a.length() / 2) + b.substring(b.length() / 2);
        tryAdd(seen, out, mixed, "Mixed alternatives not allowed together");
    }
    
    
    private void generateFallbackNegatives(Set<String> seen, List<NegativeExample> out) {
        String[] candidates = {"", " ", "x", "123", "!!!", "test"};
        
        for (String candidate : candidates) {
            if (!tryAdd(seen, out, candidate, "Does not match pattern")) {
                break;
            }
        }
    }
    
    
    private static class StructureAnalyzer implements RegexNodeVisitor<PatternStructure> {
        
        @Override
        public PatternStructure visitLiteral(LiteralNode node) {
            PatternStructure s = new PatternStructure();
            s.hasLiterals = true;
            return s;
        }
        
        @Override
        public PatternStructure visitCharClass(CharClassNode node) {
            PatternStructure s = new PatternStructure();
            s.hasCharClasses = true;
            
            for (int[] range : node.ranges()) {
                if ((range[0] >= 'a' && range[0] <= 'z') || (range[0] >= 'A' && range[0] <= 'Z')) {
                    s.hasLetters = true;
                    s.hasWordChars = true;
                }
                if (range[0] >= '0' && range[0] <= '9') {
                    s.hasDigits = true;
                    s.hasWordChars = true;
                }
            }
            
            for (int single : node.singles()) {
                if ((single >= 'a' && single <= 'z') || (single >= 'A' && single <= 'Z')) {
                    s.hasLetters = true;
                    s.hasWordChars = true;
                }
                if (single >= '0' && single <= '9') {
                    s.hasDigits = true;
                    s.hasWordChars = true;
                }
                if (single == '_') {
                    s.hasWordChars = true;
                }
            }
            
            s.isNegatedCharClass = node.negated();
            return s;
        }
        
        @Override
        public PatternStructure visitConcat(ConcatNode node) {
            PatternStructure result = new PatternStructure();
            for (RegexNode child : node.children()) {
                result = result.merge(child.accept(this));
            }
            return result;
        }
        
        @Override
        public PatternStructure visitAlternation(AlternationNode node) {
            PatternStructure result = new PatternStructure();
            result.hasAlternation = true;
            for (RegexNode alt : node.alternatives()) {
                result = result.merge(alt.accept(this));
            }
            return result;
        }
        
        @Override
        public PatternStructure visitQuantifier(QuantifierNode node) {
            PatternStructure result = node.child().accept(this);
            result.hasQuantifiers = true;
            result.quantifierMin = node.min();
            result.quantifierMax = node.max();
            return result;
        }
        
        @Override
        public PatternStructure visitGroup(GroupNode node) {
            return node.child().accept(this);
        }
        
        @Override
        public PatternStructure visitAnchor(AnchorNode node) {
            PatternStructure s = new PatternStructure();
            if (node.start()) {
                s.hasStartAnchor = true;
            } else {
                s.hasEndAnchor = true;
            }
            return s;
        }
        
        @Override
        public PatternStructure visitDot(DotNode node) {
            PatternStructure s = new PatternStructure();
            s.hasDot = true;
            return s;
        }
    }
    
    
    private static class PatternStructure {
        boolean hasLiterals = false;
        boolean hasCharClasses = false;
        boolean hasQuantifiers = false;
        boolean hasDot = false;
        boolean hasWordChars = false;
        boolean hasDigits = false;
        boolean hasLetters = false;
        boolean hasAlternation = false;
        boolean hasStartAnchor = false;
        boolean hasEndAnchor = false;
        boolean isNegatedCharClass = false;
        int quantifierMin = 0;
        Integer quantifierMax = null;
        
        PatternStructure merge(PatternStructure other) {
            PatternStructure result = new PatternStructure();
            result.hasLiterals = this.hasLiterals || other.hasLiterals;
            result.hasCharClasses = this.hasCharClasses || other.hasCharClasses;
            result.hasQuantifiers = this.hasQuantifiers || other.hasQuantifiers;
            result.hasDot = this.hasDot || other.hasDot;
            result.hasWordChars = this.hasWordChars || other.hasWordChars;
            result.hasDigits = this.hasDigits || other.hasDigits;
            result.hasLetters = this.hasLetters || other.hasLetters;
            result.hasAlternation = this.hasAlternation || other.hasAlternation;
            result.hasStartAnchor = this.hasStartAnchor || other.hasStartAnchor;
            result.hasEndAnchor = this.hasEndAnchor || other.hasEndAnchor;
            result.isNegatedCharClass = this.isNegatedCharClass || other.isNegatedCharClass;
            result.quantifierMin = Math.max(this.quantifierMin, other.quantifierMin);
            result.quantifierMax = this.quantifierMax != null ? this.quantifierMax : other.quantifierMax;
            return result;
        }
        
        @Override
        public String toString() {
            return String.format("PatternStructure{literals=%s, charClasses=%s, quantifiers=%s, dot=%s, " +
                    "wordChars=%s, digits=%s, letters=%s, alternation=%s, ^=%s, $=%s, negated=%s}",
                hasLiterals, hasCharClasses, hasQuantifiers, hasDot, 
                hasWordChars, hasDigits, hasLetters, hasAlternation,
                hasStartAnchor, hasEndAnchor, isNegatedCharClass);
        }
    }
}