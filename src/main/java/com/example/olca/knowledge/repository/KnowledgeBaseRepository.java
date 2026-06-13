package com.example.olca.knowledge.repository;


import com.example.olca.knowledge.domain.KnowledgeBase;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface KnowledgeBaseRepository extends ReactiveMongoRepository<KnowledgeBase, String> {

    /**
     * 입력된 자연어를 MongoDB text score로 계산하고 관련도가 높은 지식을 출력한다.
     */
    @Aggregation(pipeline = {
            "{ $match: { $text: { $search: ?0 } } }",
            "{ $addFields: { score: { $meta: 'textScore' } } }",
            "{ $sort: { score: -1 } }",
            "{ $limit: 10 }"
    })
    Flux<KnowledgeBase> textSearch(String searchText);

    /**
     * 입력된 키워드 배열과 겹치는 지식을 찾고 최신 버전 우선으로 출력한다.
     */
    @Aggregation(pipeline = {
            "{ $match: { keywords: { $in: ?0 } } }",
            "{ $sort: { version: -1 } }"
    })
    Flux<KnowledgeBase> searchByKeywords(List<String> keywords);

    /**
     * 입력된 topic에 해당하는 지식 중 version이 가장 높은 문서를 출력한다.
     */
    @Aggregation(pipeline = {
            "{ $match: { topic: ?0 } }",
            "{ $sort: { version: -1 } }",
            "{ $limit: 1 }"
    })
    Mono<KnowledgeBase> findLatestByTopic(String topic);

    /**
     * 입력된 topic이 저장소에 존재하는지 검증 결과를 출력한다.
     */
    Mono<Boolean> existsByTopic(String topic);
}
