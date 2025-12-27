package hu.kornel.server.domain.valueObjects.regex;


public record GroupNode(RegexNode child, boolean capturing) implements RegexNode {
    @Override
    public <T> T accept(RegexNodeVisitor<T> visitor) {
        return visitor.visitGroup(this);
    }
}