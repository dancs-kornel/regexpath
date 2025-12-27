package hu.kornel.server.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import hu.kornel.server.domain.entities.Group;
import hu.kornel.server.domain.repository.GroupRepositoryInterface;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepositoryInterface {
    
    private final GroupJpaRepository jpaRepository;
    
    @Override
    public Optional<Group> findById(Long id) {
        return jpaRepository.findById(id)
                .map(GroupJpaEntity::toDomain);
    }
    
    @Override
    public Optional<Group> findByInviteCode(String inviteCode) {
        return jpaRepository.findByInviteCode(inviteCode)
                .map(GroupJpaEntity::toDomain);
    }
    
    @Override
    public List<Group> findByTeacherId(Long teacherId) {
        return jpaRepository.findByTeacherId(teacherId).stream()
                .map(GroupJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Group> findByStudentId(Long studentId) {
        return jpaRepository.findGroupsByStudentId(studentId).stream()
                .map(GroupJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Group save(Group group) {
        GroupJpaEntity jpaEntity = GroupJpaEntity.fromDomain(group);
        GroupJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteGroupById(id);
    }
    
    @Override
    public boolean existsByInviteCode(String inviteCode) {
        return jpaRepository.existsByInviteCode(inviteCode);
    }
}