package hu.kornel.server.application.usecase.groups;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.groups.GroupDto;
import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetUserGroupsUseCase {
    
    private final GroupRepositoryInterface groupRepository;
    private final UserRepositoryInterface userRepository;
    private final GroupMembershipRepositoryInterface membershipRepository;  
    
    @Transactional(readOnly = true)
    public List<GroupDto> execute(Long userId) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedAccessException("User not found"));
        
        List<Group> groups;
        
        if (user.isTeacher()) {
            
            groups = groupRepository.findByTeacherId(userId);
        } else if (user.isStudent()) {
            
            groups = groupRepository.findByStudentId(userId);
        } else {
            
            return List.of();
        }
        
        
        return groups.stream()
        .map(group -> {
            User teacher = userRepository.findById(group.getTeacherId()).orElse(null);
            String teacherName = teacher != null ? teacher.getUsername() : "Unknown";
            int memberCount = membershipRepository.findByGroupId(group.getId()).size();
            
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
        })
        .collect(Collectors.toList());
    }
}