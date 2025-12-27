package hu.kornel.server.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import hu.kornel.server.application.dto.ExerciseDto;
import hu.kornel.server.application.dto.LessonDto;
import hu.kornel.server.application.dto.MultipleChoiceExerciseDto;
import hu.kornel.server.application.dto.OptionDto;
import hu.kornel.server.application.dto.RadioExerciseDto;
import hu.kornel.server.application.dto.RegexSandboxExerciseDto;
import hu.kornel.server.application.dto.XPathSandboxExerciseDto;
import hu.kornel.server.application.dto.publicDto.PublicExerciseDto;
import hu.kornel.server.application.dto.publicDto.PublicLessonDto;
import hu.kornel.server.application.dto.publicDto.PublicMultipleChoiceExerciseDto;
import hu.kornel.server.application.dto.publicDto.PublicOptionDto;
import hu.kornel.server.application.dto.publicDto.PublicRadioExerciseDto;
import hu.kornel.server.application.dto.publicDto.PublicRegexSandboxExerciseDto;
import hu.kornel.server.application.dto.publicDto.PublicXPathSandboxExerciseDto;
import hu.kornel.server.domain.entities.DifficultyLevel;

@Service
public class DtoMapperService {

    public PublicLessonDto toPublicLessonDto(LessonDto lessonDto, DifficultyLevel difficultyLevel) {
        PublicLessonDto publicDto = new PublicLessonDto();
        publicDto.setId(lessonDto.getId());
        publicDto.setTitle(lessonDto.getTitle());
        publicDto.setDescription(lessonDto.getDescription());
        publicDto.setIntroduction(lessonDto.getIntroduction());
        publicDto.setSummary(lessonDto.getSummary());

        List<PublicExerciseDto> publicExercises = lessonDto.getExercises().stream()
            .map(exercise -> toPublicExerciseDto(exercise, difficultyLevel))
            .collect(Collectors.toList());
        publicDto.setExercises(publicExercises);

        return publicDto;
    }

    private PublicExerciseDto toPublicExerciseDto(ExerciseDto exerciseDto, DifficultyLevel difficultyLevel) {
        if (exerciseDto instanceof MultipleChoiceExerciseDto) {
            return toPublicMultipleChoiceExerciseDto((MultipleChoiceExerciseDto) exerciseDto, difficultyLevel);
        } else if (exerciseDto instanceof RadioExerciseDto) {
            return toPublicRadioExerciseDto((RadioExerciseDto) exerciseDto, difficultyLevel);
        } else if (exerciseDto instanceof RegexSandboxExerciseDto) {
            return toPublicRegexSandboxExerciseDto((RegexSandboxExerciseDto) exerciseDto);
        } else if (exerciseDto instanceof XPathSandboxExerciseDto) {
            return toPublicXPathSandboxExerciseDto((XPathSandboxExerciseDto) exerciseDto);
        }
        throw new IllegalArgumentException("Unknown exercise type: " + exerciseDto.getClass().getName());
    }

    private PublicMultipleChoiceExerciseDto toPublicMultipleChoiceExerciseDto(
            MultipleChoiceExerciseDto exerciseDto, DifficultyLevel difficultyLevel) {

        PublicMultipleChoiceExerciseDto publicDto = new PublicMultipleChoiceExerciseDto();
        copyBaseExerciseFields(exerciseDto, publicDto);

        List<PublicOptionDto> filteredOptions = filterOptionsByDifficulty(
            exerciseDto.getOptions(), difficultyLevel);
        publicDto.setOptions(filteredOptions);

        return publicDto;
    }

    private PublicRadioExerciseDto toPublicRadioExerciseDto(
            RadioExerciseDto exerciseDto, DifficultyLevel difficultyLevel) {

        PublicRadioExerciseDto publicDto = new PublicRadioExerciseDto();
        copyBaseExerciseFields(exerciseDto, publicDto);

        List<PublicOptionDto> filteredOptions = filterOptionsByDifficulty(
            exerciseDto.getOptions(), difficultyLevel);
        publicDto.setOptions(filteredOptions);

        return publicDto;
    }

    private PublicRegexSandboxExerciseDto toPublicRegexSandboxExerciseDto(RegexSandboxExerciseDto exerciseDto) {
        PublicRegexSandboxExerciseDto publicDto = new PublicRegexSandboxExerciseDto();
        copyBaseExerciseFields(exerciseDto, publicDto);

        publicDto.setTestCases(exerciseDto.getTestCases());
        publicDto.setEnableRealTimeHighlighting(exerciseDto.getEnableRealTimeHighlighting());

        return publicDto;
    }

    private PublicXPathSandboxExerciseDto toPublicXPathSandboxExerciseDto(XPathSandboxExerciseDto exerciseDto) {
        PublicXPathSandboxExerciseDto publicDto = new PublicXPathSandboxExerciseDto();
        copyBaseExerciseFields(exerciseDto, publicDto);

        publicDto.setSampleDocument(exerciseDto.getSampleDocument());
        publicDto.setEnableRealTimeHighlighting(exerciseDto.getEnableRealTimeHighlighting());
        publicDto.setSolution(exerciseDto.getSolution());

        return publicDto;
    }

    private void copyBaseExerciseFields(ExerciseDto source, PublicExerciseDto target) {
        target.setId(source.getId());
        target.setType(source.getType());
        target.setOrder(source.getOrder());
        target.setTitle(source.getTitle());
        target.setQuestion(source.getQuestion());
        target.setInstruction(source.getInstruction());
        target.setHints(source.getHints());
        target.setExplanation(source.getExplanation());
    }

    private List<PublicOptionDto> filterOptionsByDifficulty(List<OptionDto> options, DifficultyLevel difficultyLevel) {
        List<OptionDto> correctOptions = options.stream()
            .filter(OptionDto::isCorrect)
            .collect(Collectors.toList());

        List<OptionDto> incorrectOptions = options.stream()
            .filter(opt -> !opt.isCorrect())
            .collect(Collectors.toList());

        List<OptionDto> filteredOptions = new ArrayList<>();

        filteredOptions.addAll(correctOptions);

        if (difficultyLevel == DifficultyLevel.EASY) {
            int incorrectToKeep = Math.min(2, incorrectOptions.size());
            Collections.shuffle(incorrectOptions);
            filteredOptions.addAll(incorrectOptions.subList(0, incorrectToKeep));
        } else {
            filteredOptions.addAll(incorrectOptions);
        }

        Collections.shuffle(filteredOptions);

        return filteredOptions.stream()
            .map(this::toPublicOptionDto)
            .collect(Collectors.toList());
    }

    private PublicOptionDto toPublicOptionDto(OptionDto optionDto) {
        PublicOptionDto publicDto = new PublicOptionDto();
        publicDto.setId(optionDto.getId());
        publicDto.setText(optionDto.getText());
        return publicDto;
    }
}
