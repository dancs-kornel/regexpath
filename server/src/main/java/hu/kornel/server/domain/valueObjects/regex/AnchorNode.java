package hu.kornel.server.domain.valueObjects.regex;


public record AnchorNode(boolean start) implements RegexNode {
    @Override
    public <T> T accept(RegexNodeVisitor<T> visitor) {
        return visitor.visitAnchor(this);
    }
}