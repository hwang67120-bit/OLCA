package com.example.olca.repository;

import com.example.olca.domain.ChatMessage;
import com.example.olca.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> , ChatMessageRepositoryCustom{

    List<ChatMessage> findBySessionId(Session sessionId);

}
