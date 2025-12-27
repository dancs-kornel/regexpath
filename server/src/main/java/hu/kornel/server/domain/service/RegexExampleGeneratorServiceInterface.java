package hu.kornel.server.domain.service;

import java.util.List;
import java.util.regex.Pattern;

import hu.kornel.server.domain.valueObjects.regex.RegexNode;


public interface RegexExampleGeneratorServiceInterface {
    List<String> generatePositiveExamples(RegexNode root, Pattern compiledPattern, int maxExamples);
    List<NegativeExample> generateNegativeExamples(RegexNode root, Pattern compiledPattern, int maxExamples);
    record NegativeExample(String text, String reason) {}
}