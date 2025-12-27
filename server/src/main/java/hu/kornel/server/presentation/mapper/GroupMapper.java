package hu.kornel.server.presentation.mapper;

import org.springframework.stereotype.Component;

import hu.kornel.server.application.dto.groups.CreateGroupDto;
import hu.kornel.server.application.dto.groups.CreateGroupRequest;
import hu.kornel.server.application.dto.groups.GroupDto;
import hu.kornel.server.application.dto.groups.GroupMemberDto;
import hu.kornel.server.application.dto.groups.GroupMemberResponse;
import hu.kornel.server.application.dto.groups.GroupResponse;
import hu.kornel.server.application.dto.groups.JoinGroupDto;
import hu.kornel.server.application.dto.groups.JoinGroupRequest;

@Component
public class GroupMapper {
    
    public CreateGroupDto toCreateGroupDTO(CreateGroupRequest request) {
        return CreateGroupDto.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }
    
    public JoinGroupDto toJoinGroupDTO(JoinGroupRequest request) {
        return JoinGroupDto.builder()
                .inviteCode(request.getInviteCode().toUpperCase())
                .build();
    }
    
    public GroupResponse toGroupResponse(GroupDto dto) {
        return GroupResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .inviteCode(dto.getInviteCode())
                .teacherId(dto.getTeacherId())
                .teacherName(dto.getTeacherName())
                .memberCount(dto.getMemberCount())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .active(dto.isActive())
                .build();
    }
    
    public GroupMemberResponse toGroupMemberResponse(GroupMemberDto dto) {
        return GroupMemberResponse.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .joinedAt(dto.getJoinedAt())
                .build();
    }
}