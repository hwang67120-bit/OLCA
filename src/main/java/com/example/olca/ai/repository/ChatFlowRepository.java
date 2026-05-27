package com.example.olca.ai.repository;

import com.example.olca.ai.domain.ChatFlow;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ChatFlowRepository extends ReactiveMongoRepository<ChatFlow, String> {

    // 캐싱: 같은 질문 최근 답변 조회
    @Aggregation(pipeline = {
            "{ $match: { question: ?0, userId: ?1 } }",
            "{ $sort: { createdAt: -1 } }",
            "{ $limit: 1 }"
    })
    Mono<ChatFlow> findCachedAnswer(String question, Long userId);

    // 세션별 ChatFlow 조회
    @Aggregation(pipeline = {
            "{ $match: { sessionId: ?0 } }",
            "{ $sort: { createdAt: -1 } }"
    })
    Mono<ChatFlow> findBySessionId(Long sessionId);


}
