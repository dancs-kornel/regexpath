package hu.kornel.server.domain.valueObjects.regex;


public interface RegexNodeVisitor<T> {
    T visitLiteral(LiteralNode node);
    T visitCharClass(CharClassNode node);
    T visitConcat(ConcatNode node);
    T visitAlternation(AlternationNode node);
    T visitQuantifier(QuantifierNode node);
    T visitGroup(GroupNode node);
    T visitAnchor(AnchorNode node);
    T visitDot(DotNode node);
}