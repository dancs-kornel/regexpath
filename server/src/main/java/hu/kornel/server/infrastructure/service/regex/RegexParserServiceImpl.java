package hu.kornel.server.infrastructure.service.regex;

import hu.kornel.server.domain.service.RegexParserServiceInterface;
import hu.kornel.server.domain.valueObjects.regex.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;


@Slf4j
@Service
public class RegexParserServiceImpl implements RegexParserServiceInterface {
    
    @Override
    public RegexNode parse(String pattern) {
        log.debug("Parsing regex pattern: {}", pattern);
        Parser parser = new Parser(pattern);
        RegexNode result = parser.parse();
        log.debug("Successfully parsed pattern into AST");
        return result;
    }
    
    
    private static class Parser {
        private final String pattern;
        private int pos;
        
        Parser(String pattern) {
            this.pattern = pattern;
            this.pos = 0;
        }
        
        
        RegexNode parse() {
            RegexNode result = parseAlternation();
            if (pos < pattern.length()) {
                log.warn("Parser did not consume entire pattern. Stopped at position {}", pos);
            }
            return result;
        }
        
        
        private RegexNode parseAlternation() {
            List<RegexNode> alternatives = new ArrayList<>();
            alternatives.add(parseConcat());
            
            while (peek() == '|') {
                consume(); 
                alternatives.add(parseConcat());
            }
            
            return alternatives.size() == 1 ? alternatives.get(0) : new AlternationNode(alternatives);
        }
        
        
        private RegexNode parseConcat() {
            List<RegexNode> children = new ArrayList<>();
            
            while (pos < pattern.length()) {
                char c = peek();
                
                
                if (c == '|' || c == ')') {
                    break;
                }
                
                RegexNode atom = parseAtom();
                if (atom != null) {
                    children.add(atom);
                } else {
                    break;
                }
            }
            
            if (children.isEmpty()) {
                return new LiteralNode("");
            }
            return children.size() == 1 ? children.get(0) : new ConcatNode(children);
        }
        
        
        private RegexNode parseAtom() {
            if (pos >= pattern.length()) {
                return null;
            }
            
            char c = peek();
            RegexNode base;
            
            switch (c) {
                case '\\':
                    base = parseEscape();
                    break;
                case '[':
                    base = parseCharClass();
                    break;
                case '(':
                    base = parseGroup();
                    break;
                case '^':
                    consume();
                    base = new AnchorNode(true);
                    break;
                case '$':
                    consume();
                    base = new AnchorNode(false);
                    break;
                case '.':
                    consume();
                    base = new DotNode();
                    break;
                case '*':
                case '+':
                case '?':
                case '{':
                case '|':
                case ')':
                    
                    return null;
                default:
                    base = parseLiteral();
                    break;
            }
            
            
            if (base != null && pos < pattern.length()) {
                RegexNode quantified = parseQuantifier(base);
                if (quantified != null) {
                    return quantified;
                }
            }
            
            return base;
        }
        
        
        private RegexNode parseEscape() {
            consume(); 
            if (pos >= pattern.length()) {
                return new LiteralNode("\\");
            }
            
            char escaped = consume();
            
            switch (escaped) {
                case 'd':
                    return new CharClassNode(List.of(new int[]{'0', '9'}), List.of(), false);
                case 'D':
                    return new CharClassNode(List.of(new int[]{'0', '9'}), List.of(), true);
                case 'w':
                    return new CharClassNode(
                        List.of(new int[]{'a', 'z'}, new int[]{'A', 'Z'}, new int[]{'0', '9'}),
                        List.of((int) '_'),
                        false
                    );
                case 'W':
                    return new CharClassNode(
                        List.of(new int[]{'a', 'z'}, new int[]{'A', 'Z'}, new int[]{'0', '9'}),
                        List.of((int) '_'),
                        true
                    );
                case 's':
                    return new CharClassNode(List.of(), List.of((int) ' ', (int) '\t', (int) '\n', (int) '\r'), false);
                case 'S':
                    return new CharClassNode(List.of(), List.of((int) ' ', (int) '\t', (int) '\n', (int) '\r'), true);
                default:
                    
                    return new LiteralNode(String.valueOf(escaped));
            }
        }
        
        
        private CharClassNode parseCharClass() {
            consume(); 
            
            boolean negated = false;
            if (peek() == '^') {
                negated = true;
                consume();
            }
            
            List<int[]> ranges = new ArrayList<>();
            List<Integer> singles = new ArrayList<>();
            boolean escaped = false;
            boolean firstChar = true;
            
            while (pos < pattern.length()) {
                char c = peek();
                
                if (escaped) {
                    
                    switch (c) {
                        case 'd': 
                            ranges.add(new int[]{'0', '9'});
                            break;
                        case 'w': 
                            ranges.add(new int[]{'a', 'z'});
                            ranges.add(new int[]{'A', 'Z'});
                            ranges.add(new int[]{'0', '9'});
                            singles.add((int) '_');
                            break;
                        case 's': 
                            singles.add((int) ' ');
                            singles.add((int) '\t');
                            singles.add((int) '\n');
                            singles.add((int) '\r');
                            break;
                        default:
                            
                            singles.add((int) c);
                            break;
                    }
                    consume();
                    escaped = false;
                    firstChar = false;
                    continue;
                }
                
                if (c == '\\') {
                    consume();
                    escaped = true;
                    continue;
                }
                
                if (c == ']' && !firstChar) {
                    consume();
                    break;
                }
                
                
                if (pos + 2 < pattern.length() && pattern.charAt(pos + 1) == '-' && pattern.charAt(pos + 2) != ']') {
                    int start = c;
                    consume(); 
                    consume(); 
                    int end = consume(); 
                    
                    ranges.add(new int[]{start, end});
                } else {
                    singles.add((int) c);
                    consume();
                }
                
                firstChar = false;
            }
            
            return new CharClassNode(ranges, singles, negated);
        }
        
        
        private GroupNode parseGroup() {
            consume(); 
            
            boolean capturing = true;
            
            
            if (peek() == '?') {
                if (pos + 1 < pattern.length() && pattern.charAt(pos + 1) == ':') {
                    
                    capturing = false;
                    consume(); 
                    consume(); 
                } else {
                    
                    throw new IllegalArgumentException(
                        "Unsupported construct starting with '(?' at position " + pos + 
                        ". Supported: (?:...) for non-capturing groups. " +
                        "Unsupported: (?=...), (?!...), (?<=...), (?<!...), (?i...), etc."
                    );
                }
            }
            
            RegexNode child = parseAlternation();
            
            if (peek() == ')') {
                consume();
            }
            
            return new GroupNode(child, capturing);
        }
        
        
        private LiteralNode parseLiteral() {
            StringBuilder sb = new StringBuilder();
            
            while (pos < pattern.length()) {
                char c = peek();
                
                
                if (c == '\\' || c == '[' || c == '(' || c == ')' || 
                    c == '|' || c == '*' || c == '+' || c == '?' || 
                    c == '{' || c == '^' || c == '$' || c == '.') {
                    break;
                }
                
                sb.append(consume());
            }
            
            return new LiteralNode(sb.toString());
        }
        
        
        private RegexNode parseQuantifier(RegexNode base) {
            if (pos >= pattern.length()) {
                return base;
            }
            
            char c = peek();
            
            switch (c) {
                case '*':
                    consume();
                    return QuantifierNode.star(base);
                case '+':
                    consume();
                    return QuantifierNode.plus(base);
                case '?':
                    consume();
                    return QuantifierNode.optional(base);
                case '{':
                    return parseBraceQuantifier(base);
                default:
                    return base;
            }
        }
        
        
        private RegexNode parseBraceQuantifier(RegexNode base) {
            int startPos = pos;
            consume(); 
            
            StringBuilder numStr = new StringBuilder();
            while (pos < pattern.length() && Character.isDigit(peek())) {
                numStr.append(consume());
            }
            
            if (numStr.length() == 0) {
                throw new IllegalArgumentException(
                    "Invalid quantifier at position " + startPos + ": '{' must be followed by a number"
                );
            }
            
            int min;
            try {
                min = Integer.parseInt(numStr.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Invalid quantifier at position " + startPos + ": number too large or invalid"
                );
            }
            
            if (min < 0) {
                throw new IllegalArgumentException(
                    "Invalid quantifier at position " + startPos + ": minimum cannot be negative"
                );
            }
            
            Integer max = min; 
            
            if (peek() == ',') {
                consume();
                
                
                numStr = new StringBuilder();
                while (pos < pattern.length() && Character.isDigit(peek())) {
                    numStr.append(consume());
                }
                
                if (numStr.length() > 0) {
                    try {
                        max = Integer.parseInt(numStr.toString());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                            "Invalid quantifier at position " + startPos + ": max number too large or invalid"
                        );
                    }
                    
                    if (max < 0) {
                        throw new IllegalArgumentException(
                            "Invalid quantifier at position " + startPos + ": maximum cannot be negative"
                        );
                    }
                    
                    if (max < min) {
                        throw new IllegalArgumentException(
                            "Invalid quantifier at position " + startPos + ": maximum (" + max + 
                            ") cannot be less than minimum (" + min + ")"
                        );
                    }
                } else {
                    max = null; 
                }
            }
            
            if (peek() == '}') {
                consume();
            } else {
                throw new IllegalArgumentException(
                    "Invalid quantifier at position " + startPos + ": expected '}' but found '" + peek() + "'"
                );
            }
            
            return QuantifierNode.range(base, min, max);
        }
        
        
        
        private char peek() {
            return pos < pattern.length() ? pattern.charAt(pos) : '\0';
        }
        
        private char consume() {
            return pattern.charAt(pos++);
        }
    }
}