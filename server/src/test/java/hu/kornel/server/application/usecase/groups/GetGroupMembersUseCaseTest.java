package hu.kornel.server.application.usecase.groups;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.kornel.server.application.dto.groups.GroupMemberDto;
import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.GroupMembership;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.entities.UserRole;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;

@ExtendWith(MockitoExtension.class)
class GetGroupMembersUseCaseTest {

    @Mock GroupRepositoryInterface groupRepository;
    @Mock GroupMembershipRepositoryInterface membershipRepository;
    @Mock UserRepositoryInterface userRepository;

    @InjectMocks GetGroupMembersUseCase useCase;

    @Test
    @DisplayName("returns members when requester is the teacher")
    void returnsMembers_whenRequesterIsTeacher() {
        Long groupId = 100L;
        Long teacherId = 200L;

        Group group = Group.builder()
                .id(groupId)
                .teacherId(teacherId)
                .name("regex group")
                .active(true)
                .build();

        User teacher = User.builder().id(teacherId).username("teacheruser").role(UserRole.TEACHER).active(true).build();

        GroupMembership m1 = GroupMembership.builder()
                .id(1L).groupId(groupId).studentId(11L).joinedAt(LocalDateTime.now().minusDays(1)).build();
        GroupMembership m2 = GroupMembership.builder()
                .id(2L).groupId(groupId).studentId(99L).joinedAt(LocalDateTime.now().minusHours(2)).build();

        User s1 = User.builder().id(11L).username("studentuser1").email("s1@example.com").active(true).build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher)); 
        when(membershipRepository.findByGroupId(groupId)).thenReturn(List.of(m1, m2));
        when(userRepository.findById(11L)).thenReturn(Optional.of(s1));    
        when(userRepository.findById(99L)).thenReturn(Optional.empty());    

        List<GroupMemberDto> result = useCase.execute(groupId, teacherId);

        assertThat(result).hasSize(1);
        GroupMemberDto dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUserId()).isEqualTo(11L);
        assertThat(dto.getUsername()).isEqualTo("studentuser1");
        assertThat(dto.getEmail()).isEqualTo("s1@example.com");
    }

    @Test
    @DisplayName("returns members when requester is a member (not teacher)")
    void returnsMembers_whenRequesterIsMember() {
        Long groupId = 100L;
        Long teacherId = 200L;
        Long requesterId = 11L;

        Group group = Group.builder()
                .id(groupId)
                .teacherId(teacherId) 
                .name("regex group")
                .active(true)
                .build();

        User requester = User.builder().id(requesterId).username("studentuser").role(UserRole.STUDENT).active(true).build();
        GroupMembership m1 = GroupMembership.builder()
                .id(1L).groupId(groupId).studentId(11L).joinedAt(LocalDateTime.now()).build();
        User s1 = User.builder().id(11L).username("studentuser").email("s@example.com").active(true).build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(membershipRepository.existsByGroupIdAndStudentId(groupId, requesterId)).thenReturn(true);
        when(membershipRepository.findByGroupId(groupId)).thenReturn(List.of(m1));
        when(userRepository.findById(11L)).thenReturn(Optional.of(s1));

        List<GroupMemberDto> result = useCase.execute(groupId, requesterId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("throws when group does not exist")
    void throwsWhenGroupMissing() {
        when(groupRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(100L, 1L))
                .isInstanceOf(GroupNotFoundException.class);

        verifyNoMoreInteractions(groupRepository);
        verifyNoInteractions(membershipRepository);
    }

    @Test
    @DisplayName("throws when requester user does not exist")
    void throwsWhenRequesterMissing() {
        Long groupId = 100L;

        Group group = Group.builder()
                .id(groupId)
                .teacherId(200L)
                .name("regex group")
                .build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(groupId, 1L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("User not found");

        verifyNoInteractions(membershipRepository);
    }

    @Test
    @DisplayName("throws when requester is neither teacher nor member")
    void throwsWhenNoAccess() {
        Long groupId = 100L;
        Long requesterId = 1L;

        Group group = Group.builder()
                .id(groupId)
                .teacherId(200L) 
                .name("regex group")
                .build();

        User requester = User.builder().id(requesterId).username("studentuser").role(UserRole.STUDENT).active(true).build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(membershipRepository.existsByGroupIdAndStudentId(groupId, requesterId)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(groupId, requesterId))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("don't have access");

        verify(membershipRepository, never()).findByGroupId(anyLong());
    }
}
