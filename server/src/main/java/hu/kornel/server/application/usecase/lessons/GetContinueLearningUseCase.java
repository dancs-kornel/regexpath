package hu.kornel.server.application.usecase.lessons;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.ModuleDto;
import hu.kornel.server.application.dto.lessons.ContinueLearningResponse;
import hu.kornel.server.domain.entities.LessonProgress;
import hu.kornel.server.domain.exception.UserNotFoundException;
import hu.kornel.server.domain.repository.LessonProgressRepositoryInterface;
import hu.kornel.server.domain.repository.LessonRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetContinueLearningUseCase {
	private final LessonProgressRepositoryInterface lessonProgressRepository;
	private final LessonRepositoryInterface lessonRepository;
	private final UserRepositoryInterface userRepository;

	@Transactional(readOnly = true)
	public Optional<ContinueLearningResponse> execute(Long userId) {
		userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

		return lessonProgressRepository.findLastAccessedByUserId(userId)
				.flatMap(progress -> buildContinueLearningResponse(progress));
	}

	private Optional<ContinueLearningResponse> buildContinueLearningResponse(LessonProgress progress) {
		return lessonRepository.getLessonById(progress.getLessonId())
				.flatMap(lesson -> {
					Optional<ModuleDto> moduleOpt = lessonRepository.getAllModules().stream()
							.filter(module -> lessonRepository
									.getLessonsByModuleId(module.getId())
									.stream()
									.anyMatch(l -> l.getId()
											.equals(lesson.getId())))
							.findFirst();

					return moduleOpt.map(module -> ContinueLearningResponse.builder()
							.lessonId(lesson.getId())
							.lessonTitle(lesson.getTitle())
							.moduleId(module.getId())
							.moduleTitle(module.getTitle())
							.lastAccessedAt(progress.getLastAccessedAt())
							.build());
				});
	}
}