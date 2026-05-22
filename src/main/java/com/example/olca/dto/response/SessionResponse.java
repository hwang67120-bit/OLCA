package com.example.olca.dto.response;

import com.example.olca.domain.Session;

import java.time.LocalDateTime;

public record SessionResponse(
        Long id,
        Long userId,
        String title,
        LocalDateTime createdAt
) {
    public static SessionResponse from(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getTitle(),
                session.getCreatedAt()
        );
    }
}