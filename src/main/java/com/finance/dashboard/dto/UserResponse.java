package com.finance.dashboard.dto;

import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {
    // convenient factory method — keeps controllers clean
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}