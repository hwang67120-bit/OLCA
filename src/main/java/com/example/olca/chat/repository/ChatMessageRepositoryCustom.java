package com.example.olca.chat.repository;

import com.example.olca.chat.domain.ChatMessage;

import java.util.List;

public interface ChatMessageRepositoryCustom {

    List<ChatMessage> findRecent(Long userId, Long sessionId, int limit);

}
