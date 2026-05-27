package com.example.olca.knowledge.repository;


import com.example.olca.knowledge.domain.KnowledgeBase;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface KnowledgeBaseRepository extends ReactiveMongoRepository<KnowledgeBase, String> {

    // Text Search (자연어 검색)
    @Aggregation(pipeline = {
            "{ $match: { $text: { $search: ?0 } } }",
            "{ $addFields: { score: { $meta: 'textScore' } } }",
            "{ $sort: { score: -1 } }",
            "{ $limit: 10 }"
    })
    Flux<KnowledgeBase> textSearch(String searchText);

    // Keywords 배열 검색
    @Aggregation(pipeline = {
            "{ $match: { keywords: { $in: ?0 } } }",
            "{ $sort: { version: -1 } }"
    })
    Flux<KnowledgeBase> searchByKeywords(List<String> keywords);

    // Topic의 최신 버전 조회
    @Aggregation(pipeline = {
            "{ $match: { topic: ?0 } }",
            "{ $sort: { version: -1 } }",
            "{ $limit: 1 }"
    })
    Mono<KnowledgeBase> findLatestByTopic(String topic);

    // Topic 존재 여부
    Mono<Boolean> existsByTopic(String topic);
}

