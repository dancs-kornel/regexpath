package hu.kornel.server.application.usecase.groups;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.application.dto.groups.CreateGroupDto;
import hu.kornel.server.application.dto.groups.GroupDto;
import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import hu.kornel.server.domain.valueObjects.InviteCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateGroupUseCase {
    private final GroupRepositoryInterface groupRepository;
    private final UserRepositoryInterface userRepository;

    @Transactional
    public GroupDto execute(CreateGroupDto dto, Long teacherId) {
        User teacher = userRepository.findById(teacherId).orElseThrow(() -> new UnauthorizedAccessException("User not found"));
        if (!teacher.isTeacher()) throw new UnauthorizedAccessException("Only teachers can create groups");
        
        String inviteCodeValue = generateUniqueInviteCode();

        Group group = Group.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .inviteCode(inviteCodeValue)
            .teacherId(teacherId)
            .active(true)
            .build();
        
        Group savedGroup = groupRepository.save(group);

        return GroupDto.builder()
                .id(savedGroup.getId())
                .name(savedGroup.getName())
                .description(savedGroup.getDescription())
                .inviteCode(savedGroup.getInviteCode())
                .teacherId(savedGroup.getTeacherId())
                .teacherName(teacher.getUsername())
                .memberCount(0)
                .createdAt(savedGroup.getCreatedAt())
                .updatedAt(savedGroup.getUpdatedAt())
                .active(savedGroup.isActive())
                .build();
    }

    private String generateUniqueInviteCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 10;
        
        do {
            code = InviteCode.generate().getValue();
            attempts++;  
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Failed to generate unique invite code after " + maxAttempts + " attempts");
            }
        } while (groupRepository.existsByInviteCode(code));
        
        return code;
    }
}
