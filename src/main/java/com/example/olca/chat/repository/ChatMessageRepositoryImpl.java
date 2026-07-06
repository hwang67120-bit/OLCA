package com.example.olca.chat.repository;

import com.example.olca.chat.domain.ChatMessage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.olca.chat.domain.QChatMessage.chatMessage;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessage> findRecent(Long userId, Long sessionId, int limit) {
        return queryFactory
                .selectFrom(chatMessage)
                .where(
                        chatMessage.sessionId.id.eq(sessionId),
                        chatMessage.sessionId.user.id.eq(userId)
                )
                .orderBy(chatMessage.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}
