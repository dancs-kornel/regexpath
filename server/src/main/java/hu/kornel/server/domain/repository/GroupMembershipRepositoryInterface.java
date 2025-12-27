package hu.kornel.server.domain.repository;

import java.util.List;
import java.util.Optional;

import hu.kornel.server.domain.entities.GroupMembership;

public interface GroupMembershipRepositoryInterface {
    Optional<GroupMembership> findByGroupIdAndStudentId(Long groupId, Long studentId);
    List<GroupMembership> findByGroupId(Long groupId);
    List<GroupMembership> findByStudentId(Long studentId);
    GroupMembership save(GroupMembership membership);
    void delete(GroupMembership membership);
    void deleteByGroupIdAndStudentId(Long groupId, Long studentId);
    boolean existsByGroupIdAndStudentId(Long groupId, Long studentId);
}
