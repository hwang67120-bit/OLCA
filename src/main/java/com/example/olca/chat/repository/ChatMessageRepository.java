package com.example.olca.chat.repository;

import com.example.olca.chat.domain.ChatMessage;
import com.example.olca.session.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> , ChatMessageRepositoryCustom{

    List<ChatMessage> findBySessionId(Session sessionId);

}
