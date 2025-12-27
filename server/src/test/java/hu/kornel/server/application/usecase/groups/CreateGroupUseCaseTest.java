package hu.kornel.server.application.usecase.groups;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.kornel.server.application.dto.groups.CreateGroupDto;
import hu.kornel.server.application.dto.groups.GroupDto;
import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.UserRole;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;

@ExtendWith(MockitoExtension.class)
class CreateGroupUseCaseTest {

    @Mock
    GroupRepositoryInterface groupRepository;
    @Mock
    UserRepositoryInterface userRepository;
    @InjectMocks
    CreateGroupUseCase useCase;

    @Test
    @DisplayName("teacher creates group -> group saved and DTO returned")
    void createsGroup_successfully() {
        Long teacherId = 1L;
        User teacher = User.builder()
                .id(teacherId)
                .username("professor")
                .role(UserRole.TEACHER)
                .active(true)
                .build();

        CreateGroupDto dto = new CreateGroupDto("Regex group", "Learn regex fast");

        Group saved = Group.builder()
                .id(10L)
                .name(dto.getName())
                .description(dto.getDescription())
                .teacherId(teacherId)
                .inviteCode("ABC123")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(groupRepository.existsByInviteCode(anyString())).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenReturn(saved);

        GroupDto result = useCase.execute(dto, teacherId);

        assertThat(result)
                .extracting(GroupDto::getName, GroupDto::getDescription, GroupDto::getTeacherId,
                        GroupDto::getTeacherName, GroupDto::isActive)
                .containsExactly("Regex group", "Learn regex fast", teacherId, "professor", true);

        assertThat(result.getInviteCode()).matches("^[A-Z0-9]{6}$");
        verify(groupRepository).save(any(Group.class));
        verifyNoMoreInteractions(groupRepository);
    }

    @Test
    @DisplayName("User not found -> UnauthorizedAccessException")
    void userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        CreateGroupDto dto = new CreateGroupDto("Test", "desc");

        assertThatThrownBy(() -> useCase.execute(dto, 1L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(1L);
        verifyNoInteractions(groupRepository);
    }

    @Test
    @DisplayName("User is not a teacher -> UnauthorizedAccessException('Only teachers...')")
    void nonTeacher_throws() {
        Long id = 1L;
        User user = User.builder().id(id).role(UserRole.STUDENT).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        CreateGroupDto dto = new CreateGroupDto("Group", "desc");

        assertThatThrownBy(() -> useCase.execute(dto, id))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Only teachers");

        verify(userRepository).findById(id);
        verifyNoInteractions(groupRepository);
    }

    @Test
    @DisplayName("Retries generating invite code until unique one found")
    void retriesUntilUniqueCode() {
        Long teacherId = 1L;
        User teacher = User.builder().id(teacherId).role(UserRole.TEACHER).build();
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        when(groupRepository.existsByInviteCode(anyString()))
                .thenReturn(true, true, false);

        when(groupRepository.save(any(Group.class)))
                .thenAnswer(inv -> {
                    Group g = inv.getArgument(0);
                    g.setId(5L);
                    return g;
                });

        CreateGroupDto dto = new CreateGroupDto("Regex group", "desc");
        GroupDto result = useCase.execute(dto, teacherId);

        assertThat(result).isNotNull();
        verify(groupRepository, atLeast(3)).existsByInviteCode(anyString());
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    @DisplayName("Throws RuntimeException after too many duplicate invite codes")
    void throwsAfterMaxAttempts() {
        Long teacherId = 1L;
        User teacher = User.builder().id(teacherId).role(UserRole.TEACHER).build();
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        when(groupRepository.existsByInviteCode(anyString())).thenReturn(true);

        CreateGroupDto dto = new CreateGroupDto("FailGroup", "desc");

        assertThatThrownBy(() -> useCase.execute(dto, teacherId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate unique invite code");

        verify(groupRepository, times(9)).existsByInviteCode(anyString());
        verify(groupRepository, never()).save(any());
    }

}
