package com.example.olca.repository;

import com.example.olca.domain.ChatMessage;

import java.util.List;

public interface ChatMessageRepositoryCustom {

    List<ChatMessage> findRecent(Long userId, Long sessionId, int limit);

}
