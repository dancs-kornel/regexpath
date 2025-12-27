package hu.kornel.server.application.usecase.groups;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.groups.GroupDto;
import hu.kornel.server.application.dto.groups.JoinGroupDto;
import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.GroupMembership;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.AlreadyMemberException;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JoinGroupUseCase {
    
    private final GroupRepositoryInterface groupRepository;
    private final GroupMembershipRepositoryInterface membershipRepository;
    private final UserRepositoryInterface userRepository;
    
    @Transactional
    public GroupDto execute(JoinGroupDto dto, Long studentId) {
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UnauthorizedAccessException("User not found"));
        
        if (!student.isStudent()) {
            throw new UnauthorizedAccessException("Only students can join groups");
        }
        
        Group group = groupRepository.findByInviteCode(dto.getInviteCode())
                .orElseThrow(() -> new GroupNotFoundException(dto.getInviteCode()));
        
        if (membershipRepository.existsByGroupIdAndStudentId(group.getId(), studentId)) {
            throw new AlreadyMemberException(studentId, group.getId());
        }
        
        GroupMembership membership = GroupMembership.builder()
                .groupId(group.getId())
                .studentId(studentId)
                .build();
        
        membershipRepository.save(membership);
        
        int memberCount = membershipRepository.findByGroupId(group.getId()).size();
        
        User teacher = userRepository.findById(group.getTeacherId()).orElse(null);
        String teacherName = teacher != null ? teacher.getUsername() : "Unknown";
        
        return GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .inviteCode(group.getInviteCode())
                .teacherId(group.getTeacherId())
                .teacherName(teacherName)
                .memberCount(memberCount)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .active(group.isActive())
                .build();
    }
}