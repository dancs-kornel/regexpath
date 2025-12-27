package hu.kornel.server.application.usecase.assignments;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.assignments.TeacherAttemptViewResponse;
import hu.kornel.server.application.dto.assignments.TeacherAttemptViewResponse.TeacherExerciseAnswerView;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.assignments.Assignment;
import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.entities.assignments.Exercise;
import hu.kornel.server.domain.entities.assignments.ExerciseAnswer;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseAnswerRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetTeacherAttemptViewUseCase {
    
    private final AssignmentAttemptRepositoryInterface attemptRepository;
    private final AssignmentRepositoryInterface assignmentRepository;
    private final ExerciseRepositoryInterface exerciseRepository;
    private final ExerciseAnswerRepositoryInterface answerRepository;
    private final UserRepositoryInterface userRepository;
    
    @Transactional(readOnly = true)
    public TeacherAttemptViewResponse execute(Long attemptId, Long teacherId) {
        log.debug("Teacher {} viewing attempt {}", teacherId, attemptId);
        
        
        AssignmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalStateException("Próbálkozás nem található"));
        
        
        Assignment assignment = assignmentRepository.findById(attempt.getAssignmentId())
                .orElseThrow(() -> new IllegalStateException("Feladat nem található"));
        
        if (!assignment.isOwnedBy(teacherId)) {
            throw new UnauthorizedAccessException("Csak a saját feladataid diákjainak próbálkozásait tekintheted meg");
        }
        
        
        User student = userRepository.findById(attempt.getStudentId())
                .orElseThrow(() -> new UserNotFoundException(""));
        
        
        List<ExerciseAnswer> answers = answerRepository.findByAttemptId(attemptId);
        
        List<TeacherExerciseAnswerView> answerViews = answers.stream()
                .map(answer -> buildAnswerView(answer))
                .collect(Collectors.toList());
        
        log.info("Teacher {} viewing attempt {} by student {}: {} answers", 
                teacherId, attemptId, student.getId(), answerViews.size());
        
        return TeacherAttemptViewResponse.builder()
                .studentId(student.getId())
                .studentName(student.getUsername())
                .studentEmail(student.getEmail())
                .assignmentId(assignment.getId())
                .assignmentTitle(assignment.getTitle())
                .totalPoints(assignment.getTotalPoints())
                .attemptId(attempt.getId())
                .attemptNumber(attempt.getAttemptNumber())
                .score(attempt.getScore())
                .maxScore(attempt.getMaxScore())
                .percentageScore(attempt.getPercentageScore())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .timeSpentMinutes(attempt.getTimeSpentMinutes())
                .completed(attempt.isCompleted())
                .expired(attempt.isExpired())
                .answers(answerViews)
                .build();
    }
    
    private TeacherExerciseAnswerView buildAnswerView(ExerciseAnswer answer) {
        
        Exercise exercise = exerciseRepository.findById(answer.getExerciseId()).orElse(null);
        
        return TeacherExerciseAnswerView.builder()
                .answerId(answer.getId())
                .exerciseId(answer.getExerciseId())
                .exerciseTitle(exercise != null ? exercise.getTitle() : "")
                .exerciseQuestion(exercise != null ? exercise.getQuestion() : "")
                .exerciseType(answer.getExerciseType().toString())
                .studentAnswerJson(answer.getAnswerJson())
                .correct(answer.getCorrect())
                .pointsEarned(answer.getPointsEarned())
                .pointsPossible(answer.getPointsPossible())
                .validationResultJson(answer.getValidationResultJson())
                .exerciseConfigJson(exercise != null ? exercise.getConfigJson() : null)
                .build();
    }
}