package hu.kornel.server.application.usecase.groups;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveGroupUseCase {
    
    private final GroupRepositoryInterface groupRepository;
    private final GroupMembershipRepositoryInterface membershipRepository;
    private final UserRepositoryInterface userRepository;
    
    @Transactional
    public void execute(Long groupId, Long studentId) {
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UnauthorizedAccessException("User not found"));
        
        if (!student.isStudent()) {
            throw new UnauthorizedAccessException("Only students can leave groups");
        }
        
        
        groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        
        
        if (!membershipRepository.existsByGroupIdAndStudentId(groupId, studentId)) {
            throw new UnauthorizedAccessException("You are not a member of this group");
        }

        membershipRepository.deleteByGroupIdAndStudentId(groupId, studentId);
    }
}