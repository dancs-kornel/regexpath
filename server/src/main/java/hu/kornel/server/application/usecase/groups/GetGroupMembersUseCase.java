package hu.kornel.server.application.usecase.groups;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.groups.GroupMemberDto;
import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.GroupMembership;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetGroupMembersUseCase {

    private final GroupRepositoryInterface groupRepository;
    private final GroupMembershipRepositoryInterface membershipRepository;
    private final UserRepositoryInterface userRepository;

    @Transactional(readOnly = true)
    public List<GroupMemberDto> execute(Long groupId, Long requestingUserId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
        if (!userRepository.findById(requestingUserId).isPresent()) throw new UnauthorizedAccessException("User not found");
        
        boolean isTeacher = group.isOwnedBy(requestingUserId);
        boolean isMember = membershipRepository.existsByGroupIdAndStudentId(groupId, requestingUserId);

        if (!isTeacher && !isMember) throw new UnauthorizedAccessException("You don't have access to this group");
        
        List<GroupMembership> memberships = membershipRepository.findByGroupId(groupId);

        return memberships.stream().map(membership -> {
            User student = userRepository.findById(membership.getStudentId()).orElse(null);
            if (student == null)
                return null;
            return GroupMemberDto.builder()
                    .id(membership.getId())
                    .userId(student.getId())
                    .username(student.getUsername())
                    .email(student.getEmail())
                    .joinedAt(membership.getJoinedAt())
                    .build();
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }
}