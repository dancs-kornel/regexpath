package hu.kornel.server.domain.service;

import hu.kornel.server.domain.valueObjects.regex.RegexNode;


public interface RegexParserServiceInterface {
    RegexNode parse(String pattern);
}