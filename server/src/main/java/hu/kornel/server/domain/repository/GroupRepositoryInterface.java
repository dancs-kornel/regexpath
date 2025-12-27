package hu.kornel.server.domain.repository;

import java.util.List;
import java.util.Optional;

import hu.kornel.server.domain.entities.Group;

public interface GroupRepositoryInterface {
    Optional<Group> findById(Long id);
    Optional<Group> findByInviteCode(String inviteCode);
    List<Group> findByTeacherId(Long teacherId);
    List<Group> findByStudentId(Long studentId);
    Group save(Group group);
    void deleteById(Long id);
    boolean existsByInviteCode(String inviteCode);
}
