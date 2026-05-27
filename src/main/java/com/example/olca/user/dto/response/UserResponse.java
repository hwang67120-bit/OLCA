package com.example.olca.user.dto.response;

import com.example.olca.user.domain.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt()
        );
    }
}