package hu.kornel.server.application.usecase.groups;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.GroupNotFoundException;
import hu.kornel.server.domain.exception.UnauthorizedAccessException;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import hu.kornel.server.domain.repository.UserRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteGroupUseCase {
    
    private final GroupRepositoryInterface groupRepository;
    private final UserRepositoryInterface userRepository;
    
    @Transactional
    public void execute(Long groupId, Long teacherId) {
        User teacher = userRepository.findById(teacherId).orElseThrow(() -> new UnauthorizedAccessException("User not found"));
        if (!teacher.isTeacher()) throw new UnauthorizedAccessException("Only teachers can delete groups");
        
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
        if (!group.isOwnedBy(teacherId)) throw new UnauthorizedAccessException("You can only delete your own groups");
    
        groupRepository.deleteById(groupId);
    }
}