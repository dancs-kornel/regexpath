package hu.kornel.server.domain.valueObjects.regex;


public record DotNode() implements RegexNode {
    @Override
    public <T> T accept(RegexNodeVisitor<T> visitor) {
        return visitor.visitDot(this);
    }
}