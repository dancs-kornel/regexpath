package hu.kornel.server.domain.valueObjects.regex;

import java.util.List;


public record AlternationNode(List<RegexNode> alternatives) implements RegexNode {
    @Override
    public <T> T accept(RegexNodeVisitor<T> visitor) {
        return visitor.visitAlternation(this);
    }
}