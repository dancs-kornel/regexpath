package hu.kornel.server.infrastructure.service.regex;

import hu.kornel.server.domain.service.RegexExampleGeneratorServiceInterface;
import hu.kornel.server.domain.valueObjects.regex.RegexNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;


@Slf4j
@Service
public class RegexExampleGeneratorServiceImpl implements RegexExampleGeneratorServiceInterface {
    
    @Override
    public List<String> generatePositiveExamples(RegexNode root, Pattern compiledPattern, int maxExamples) {
        log.debug("Generating up to {} positive examples", maxExamples);
        PositiveExampleGenerator generator = new PositiveExampleGenerator(compiledPattern, maxExamples);
        return generator.generate(root);
    }
    
    @Override
    public List<NegativeExample> generateNegativeExamples(RegexNode root, Pattern compiledPattern, int maxExamples) {
        log.debug("Generating up to {} negative examples", maxExamples);
        NegativeExampleGenerator generator = new NegativeExampleGenerator(root, compiledPattern, maxExamples);
        return generator.generateNegativeExamples();
    }
}