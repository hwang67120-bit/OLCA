package com.example.olca.chat.dto.response;

import com.example.olca.chat.domain.ChatMessage;

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
                message.getSessionId().getId(),
                message.getQuestion(),
                message.getAnswer(),
                message.getCreatedAt()

        );
    }
}
