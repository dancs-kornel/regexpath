package hu.kornel.server.domain.valueObjects.regex;


public record LiteralNode(String value) implements RegexNode {
    @Override
    public <T> T accept(RegexNodeVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }
}