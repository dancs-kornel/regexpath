package hu.kornel.server.application.usecase.assignments;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.assignments.AssignmentAttempt;
import hu.kornel.server.domain.entities.assignments.ExerciseAnswer;
import hu.kornel.server.domain.repository.assignments.AssignmentAttemptRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.AssignmentRepositoryInterface;
import hu.kornel.server.domain.repository.assignments.ExerciseAnswerRepositoryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetAttemptResultsUseCase {
    
    private final AssignmentAttemptRepositoryInterface attemptRepository;
    private final AssignmentRepositoryInterface assignmentRepository;
    private final ExerciseAnswerRepositoryInterface answerRepository;
    
    @Transactional(readOnly = true)
    public AssignmentAttempt execute(Long attemptId, Long userId) {
        log.debug("Fetching attempt results for attempt {} by user {}", attemptId, userId);
        
        AssignmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalStateException("Próbálkozás nem található"));
        
        if (!attempt.getStudentId().equals(userId)) {
            throw new IllegalStateException("Csak a saját próbálkozásaidat tekintheted meg");
        }
        
        List<ExerciseAnswer> answers = answerRepository.findByAttemptId(attemptId);
        attempt.setAnswers(answers);
        
        log.info("Attempt {} results fetched with {} answers", attemptId, answers.size());
        
        return attempt;
    }
}