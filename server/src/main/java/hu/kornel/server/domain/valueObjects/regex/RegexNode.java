package hu.kornel.server.domain.valueObjects.regex;


public interface RegexNode {
    
    <T> T accept(RegexNodeVisitor<T> visitor);
}