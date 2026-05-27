package com.example.olca.chat.repository;

import com.example.olca.chat.domain.ChatMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ChatMessage> findRecent(Long userId, Long sessionId, int limit) {
        return em.createQuery(
                        "SELECT m FROM ChatMessage m " +
                                "WHERE m.sessionId.id = :sessionId " +  
                                "ORDER BY m.createdAt DESC",
                        ChatMessage.class)
                .setParameter("sessionId", sessionId)
                .setMaxResults(limit)
                .getResultList();
    }
}
