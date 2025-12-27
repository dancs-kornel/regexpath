package hu.kornel.server.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hu.kornel.server.domain.entities.GroupMembership;
import hu.kornel.server.domain.repository.GroupMembershipRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GroupMembershipRepositoryImpl implements GroupMembershipRepositoryInterface {
    
    private final GroupMembershipJpaRepository jpaRepository;
    
    @Override
    public Optional<GroupMembership> findByGroupIdAndStudentId(Long groupId, Long studentId) {
        return jpaRepository.findByGroupIdAndStudentId(groupId, studentId)
                .map(GroupMembershipJpaEntity::toDomain);
    }
    
    @Override
    public List<GroupMembership> findByGroupId(Long groupId) {
        return jpaRepository.findByGroupId(groupId).stream()
                .map(GroupMembershipJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<GroupMembership> findByStudentId(Long studentId) {
        return jpaRepository.findByStudentId(studentId).stream()
                .map(GroupMembershipJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public GroupMembership save(GroupMembership membership) {
        GroupMembershipJpaEntity jpaEntity = GroupMembershipJpaEntity.fromDomain(membership);
        GroupMembershipJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    @Transactional
    public void delete(GroupMembership membership) {
        jpaRepository.deleteById(membership.getId());
    }
    
    @Override
    @Transactional
    public void deleteByGroupIdAndStudentId(Long groupId, Long studentId) {
        jpaRepository.deleteByGroupIdAndStudentId(groupId, studentId);
    }
    
    @Override
    public boolean existsByGroupIdAndStudentId(Long groupId, Long studentId) {
        return jpaRepository.existsByGroupIdAndStudentId(groupId, studentId);
    }
}