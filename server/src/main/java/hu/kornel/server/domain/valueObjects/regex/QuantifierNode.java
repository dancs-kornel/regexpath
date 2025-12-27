package hu.kornel.server.domain.valueObjects.regex;


public record QuantifierNode(RegexNode child, int min, Integer max) implements RegexNode {
    @Override
    public <T> T accept(RegexNodeVisitor<T> visitor) {
        return visitor.visitQuantifier(this);
    }
    
    public static QuantifierNode star(RegexNode child) {
        return new QuantifierNode(child, 0, null);
    }
    
    public static QuantifierNode plus(RegexNode child) {
        return new QuantifierNode(child, 1, null);
    }
    
    public static QuantifierNode optional(RegexNode child) {
        return new QuantifierNode(child, 0, 1);
    }
    
    public static QuantifierNode exact(RegexNode child, int n) {
        return new QuantifierNode(child, n, n);
    }
    
    public static QuantifierNode range(RegexNode child, int min, Integer max) {
        return new QuantifierNode(child, min, max);
    }
}