package hu.kornel.server.application.usecase.groups;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.kornel.server.application.dto.groups.GroupDto;
import hu.kornel.server.application.dto.groups.JoinGroupDto;
import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.GroupMembership;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.UserRole;
import hu.kornel.server.domain.exception.AlreadyMemberException;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;

@ExtendWith(MockitoExtension.class)
class JoinGroupUseCaseTest {

    @Mock
    GroupRepositoryInterface groupRepository;
    @Mock
    GroupMembershipRepositoryInterface membershipRepository;
    @Mock
    UserRepositoryInterface userRepository;

    @InjectMocks
    JoinGroupUseCase useCase;

    @Test
    @DisplayName("joins group when student and invite code are valid")
    void joinsGroup_success() {
        Long studentId = 11L;
        Long teacherId = 22L;
        Long groupId = 33L;

        var student = User.builder().id(studentId).username("studentuser").role(UserRole.STUDENT).active(true).build();
        var teacher = User.builder().id(teacherId).username("teacheruser").role(UserRole.TEACHER).active(true).build();

        var group = Group.builder()
                .id(groupId)
                .name("regex group")
                .description("desc")
                .inviteCode("ABC123")
                .teacherId(teacherId)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        JoinGroupDto dto = mock(JoinGroupDto.class);
        when(dto.getInviteCode()).thenReturn("ABC123");

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(groupRepository.findByInviteCode("ABC123")).thenReturn(Optional.of(group));
        when(membershipRepository.existsByGroupIdAndStudentId(groupId, studentId)).thenReturn(false);
        when(membershipRepository.save(any(GroupMembership.class))).thenAnswer(inv -> inv.getArgument(0));
        when(membershipRepository.findByGroupId(groupId)).thenReturn(
                List.of(
                        GroupMembership.builder().groupId(groupId).studentId(studentId).build(),
                        GroupMembership.builder().groupId(groupId).studentId(99L).build()));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        GroupDto result = useCase.execute(dto, studentId);

        assertThat(result)
                .extracting(GroupDto::getId, GroupDto::getName, GroupDto::getInviteCode, GroupDto::getTeacherId,
                        GroupDto::getTeacherName, GroupDto::getMemberCount)
                .containsExactly(groupId, "regex group", "ABC123", teacherId, "teacheruser", 2);

        verify(membershipRepository)
                .save(argThat(m -> m.getGroupId().equals(groupId) && m.getStudentId().equals(studentId)));
    }

    @Test
    @DisplayName("throws when user is not found")
    void userNotFound_throws() {
        Long studentId = 11L;
        JoinGroupDto dto = mock(JoinGroupDto.class);
        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(dto, studentId))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(studentId);
        verifyNoInteractions(groupRepository, membershipRepository);
    }

    @Test
    @DisplayName("throws when user is not a student")
    void notStudent_throws() {
        Long studentId = 11L;
        var user = User.builder().id(studentId).username("teacheruser").role(UserRole.TEACHER).active(true).build();
        when(userRepository.findById(studentId)).thenReturn(Optional.of(user));

        JoinGroupDto dto = mock(JoinGroupDto.class);

        assertThatThrownBy(() -> useCase.execute(dto, studentId))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Only students");

        verify(userRepository).findById(studentId);
        verifyNoInteractions(groupRepository, membershipRepository);
    }

    @Test
    @DisplayName("throws when group is not found by invite code")
    void groupNotFound_throws() {
        Long studentId = 11L;
        var student = User.builder().id(studentId).role(UserRole.STUDENT).active(true).build();

        JoinGroupDto dto = mock(JoinGroupDto.class);
        when(dto.getInviteCode()).thenReturn("BAD999");

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(groupRepository.findByInviteCode("BAD999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(dto, studentId))
                .isInstanceOf(GroupNotFoundException.class);

        verify(groupRepository).findByInviteCode("BAD999");
        verifyNoMoreInteractions(groupRepository);
        verifyNoInteractions(membershipRepository);
    }

    @Test
    @DisplayName("throws when user is already a member")
    void alreadyMember_throws() {
        Long studentId = 11L;
        Long groupId = 33L;
        var student = User.builder().id(studentId).role(UserRole.STUDENT).active(true).build();
        var group = Group.builder().id(groupId).inviteCode("ABC123").teacherId(22L).active(true).build();

        JoinGroupDto dto = mock(JoinGroupDto.class);
        when(dto.getInviteCode()).thenReturn("ABC123");

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(groupRepository.findByInviteCode("ABC123")).thenReturn(Optional.of(group));
        when(membershipRepository.existsByGroupIdAndStudentId(groupId, studentId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(dto, studentId))
                .isInstanceOf(AlreadyMemberException.class);

        verify(membershipRepository, never()).save(any());
        verify(membershipRepository, never()).findByGroupId(anyLong());
    }

    @Test
    @DisplayName("sets teacherName to Unknown when teacher record is missing")
    void teacherUnknown_setsFallbackName() {
        Long studentId = 11L;
        Long teacherId = 22L;
        Long groupId = 33L;

        var student = User.builder().id(studentId).username("studentuser").role(UserRole.STUDENT).active(true).build();
        var group = Group.builder().id(groupId).name("regex group").inviteCode("ABC123").teacherId(teacherId)
                .active(true).build();

        JoinGroupDto dto = mock(JoinGroupDto.class);
        when(dto.getInviteCode()).thenReturn("ABC123");

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(groupRepository.findByInviteCode("ABC123")).thenReturn(Optional.of(group));
        when(membershipRepository.existsByGroupIdAndStudentId(groupId, studentId)).thenReturn(false);
        when(membershipRepository.save(any(GroupMembership.class))).thenAnswer(inv -> inv.getArgument(0));
        when(membershipRepository.findByGroupId(groupId)).thenReturn(List.of());
        when(userRepository.findById(teacherId)).thenReturn(Optional.empty()); 

        GroupDto result = useCase.execute(dto, studentId);

        assertThat(result.getTeacherName()).isEqualTo("Unknown");
        assertThat(result.getMemberCount()).isEqualTo(0);
    }
}
