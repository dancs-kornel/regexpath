package hu.kornel.server.presentation.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.kornel.server.application.dto.groups.CreateGroupRequest;
import hu.kornel.server.application.dto.groups.GroupDto;
import hu.kornel.server.application.dto.groups.GroupMemberDto;
import hu.kornel.server.application.dto.groups.GroupMemberResponse;
import hu.kornel.server.application.dto.groups.GroupResponse;
import hu.kornel.server.application.dto.groups.JoinGroupRequest;
import hu.kornel.server.application.usecase.groups.CreateGroupUseCase;
import hu.kornel.server.application.usecase.groups.DeleteGroupUseCase;
import hu.kornel.server.application.usecase.groups.GetGroupMembersUseCase;
import hu.kornel.server.application.usecase.groups.GetUserGroupsUseCase;
import hu.kornel.server.application.usecase.groups.JoinGroupUseCase;
import hu.kornel.server.application.usecase.groups.LeaveGroupUseCase;
import hu.kornel.server.infrastructure.security.UserPrincipal;
import hu.kornel.server.presentation.mapper.GroupMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {
    private final CreateGroupUseCase createGroupUseCase;
    private final JoinGroupUseCase joinGroupUseCase;
    private final GetUserGroupsUseCase getUserGroupsUseCase;
    private final GetGroupMembersUseCase getGroupMembersUseCase;
    private final DeleteGroupUseCase deleteGroupUseCase;
    private final LeaveGroupUseCase leaveGroupUseCase;
    private final GroupMapper groupMapper;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        GroupDto groupDTO = createGroupUseCase.execute(groupMapper.toCreateGroupDTO(request), userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(groupMapper.toGroupResponse(groupDTO));
    }
    
    @PostMapping("/join")
    public ResponseEntity<GroupResponse> joinGroup(@Valid @RequestBody JoinGroupRequest request, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        GroupDto groupDTO = joinGroupUseCase.execute(groupMapper.toJoinGroupDTO(request), userPrincipal.getId());
        return ResponseEntity.ok(groupMapper.toGroupResponse(groupDTO));
    }
    
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getUserGroups(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<GroupDto> groups = getUserGroupsUseCase.execute(userPrincipal.getId());
        List<GroupResponse> response = groups.stream().map(groupMapper::toGroupResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable Long groupId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<GroupMemberDto> members = getGroupMembersUseCase.execute(groupId, userPrincipal.getId());
        List<GroupMemberResponse> response = members.stream().map(groupMapper::toGroupMemberResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        deleteGroupUseCase.execute(groupId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId,@AuthenticationPrincipal UserPrincipal userPrincipal) {
        leaveGroupUseCase.execute(groupId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
