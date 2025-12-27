package hu.kornel.server.domain.valueObjects.regex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public record CharClassNode(List<int[]> ranges, List<Integer> singles, boolean negated) implements RegexNode {
    @Override
    public <T> T accept(RegexNodeVisitor<T> visitor) {
        return visitor.visitCharClass(this);
    }
    
    
    public List<Character> sampleCharacters(int maxSamples) {
        if (negated) {
            
            Set<Integer> excluded = new HashSet<>(singles);
            for (int[] range : ranges) {
                for (int c = range[0]; c <= range[1]; c++) {
                    excluded.add(c);
                }
            }
            
            char[] fallbackAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 !@#$_-".toCharArray();
            List<Character> result = new ArrayList<>();
            
            for (char c : fallbackAlphabet) {
                if (!excluded.contains((int) c)) {
                    result.add(c);
                    if (result.size() >= maxSamples) break;
                }
            }
            
            
            if (result.isEmpty()) {
                result.add('\u0000');
            }
            
            return result;
        } else {
            
            List<Character> result = new ArrayList<>();
            
            
            for (int[] range : ranges) {
                int start = range[0];
                int end = range[1];
                
                result.add((char) start);
                if (end > start) {
                    result.add((char) end);
                }
                if (end > start + 1) {
                    result.add((char) ((start + end) / 2));
                }
                
                if (result.size() >= maxSamples) break;
            }
            
            
            for (int single : singles) {
                result.add((char) single);
                if (result.size() >= maxSamples) break;
            }
            
            return result;
        }
    }
}