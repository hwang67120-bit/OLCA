package com.example.olca.dto.response;

import com.example.olca.domain.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long sessionId,
        String question,
        String answer,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message){
        return new ChatMessageResponse(
                message.getId(),
                message.getSession().getId(),
                message.getQuestion(),
                message.getAnswer(),
                message.getCreatedAt()

        );
    }
}
