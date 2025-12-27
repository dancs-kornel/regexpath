package hu.kornel.server.application.mapper;

import hu.kornel.server.application.dto.UserDto;
import hu.kornel.server.domain.entities.User;


public final class UserMapper {

    private UserMapper() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    
    public static UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .active(user.isActive())
                .build();
    }
}
