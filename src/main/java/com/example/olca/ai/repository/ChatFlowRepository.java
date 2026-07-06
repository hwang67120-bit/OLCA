package com.example.olca.ai.repository;

import com.example.olca.ai.domain.ChatFlow;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ChatFlowRepository extends ReactiveMongoRepository<ChatFlow, String> {

    /**
     * 입력된 질문과 사용자 ID로 캐시 가능한 최신 RAG 답변만 조회한다.
     */
    @Aggregation(pipeline = {
            "{ $match: { question: ?0, userId: ?1, cacheable: true } }",
            "{ $sort: { createAt: -1 } }",
            "{ $limit: 1 }"
    })
    Mono<ChatFlow> findCachedAnswer(String question, Long userId);

    /**
     * 입력된 세션 ID로 대화 흐름을 조회하고 최신 흐름부터 출력한다.
     */
    @Aggregation(pipeline = {
            "{ $match: { sessionId: ?0 } }",
            "{ $sort: { createAt: -1 } }"
    })
    Mono<ChatFlow> findBySessionId(Long sessionId);


}
