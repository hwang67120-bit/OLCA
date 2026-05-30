package com.example.olca.knowledge.service;


import com.example.olca.ai.service.EmbeddingService;
import com.example.olca.knowledge.domain.KnowledgeBase;
import com.example.olca.knowledge.dto.response.KnowledgeVectorSearchResponse;
import com.example.olca.knowledge.repository.KnowledgeBaseRepository;
import com.example.olca.session.dto.response.KnowledgeBaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final EmbeddingService embeddingService;

    // ✅ 문서 저장 + 임베딩 자동 생성
    @Transactional
    public Mono<KnowledgeBaseResponse> saveWithEmbedding(
            String topic,
            String content,
            List<String> keywords
    ) {
        List<String> safeKeywords = keywords == null ? List.of() : keywords;

        return Mono.fromCallable(() ->
                        embeddingService.embed(topic + " " + content)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(embedding ->
                        knowledgeBaseRepository.findLatestByTopic(topic)
                                .flatMap(existing -> {
                                    KnowledgeBase newVersion = KnowledgeBase.builder()
                                            .topic(topic)
                                            .content(content)
                                            .keywords(safeKeywords)
                                            .embedding(embedding)
                                            .version(existing.getVersion() + 1)
                                            .build();
                                    return knowledgeBaseRepository.save(newVersion);
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    KnowledgeBase newKb = KnowledgeBase.builder()
                                            .topic(topic)
                                            .content(content)
                                            .keywords(safeKeywords)
                                            .embedding(embedding)
                                            .version(1)
                                            .build();
                                    return knowledgeBaseRepository.save(newKb);
                                }))
                )
                .map(KnowledgeBaseResponse::from);
    }

    // ✅ 벡터 유사도 검색
    public Mono<List<KnowledgeBase>> vectorSearch(String question, int topN) {
        return Mono.fromCallable(() ->
                        embeddingService.embed(question)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(questionVector ->
                        knowledgeBaseRepository.findAll()
                                .filter(kb -> kb.getEmbedding() != null && !kb.getEmbedding().isEmpty())
                                .collectList()
                                .map(all -> all.stream()
                                        .collect(Collectors.toMap(
                                                KnowledgeBase::getTopic,
                                                Function.identity(),
                                                (a, b) -> a.getVersion() >= b.getVersion() ? a : b
                                        ))
                                        .values()
                                        .stream()
                                        .sorted((a, b) -> Double.compare(
                                                cosineSimilarity(b.getEmbedding(), questionVector),
                                                cosineSimilarity(a.getEmbedding(), questionVector)
                                        ))
                                        .limit(topN)
                                        .toList()
                                )
                )
                .doOnSuccess(results ->
                        log.info("벡터 검색 결과: {}건", results.size())
                );
    }

    // ✅ 코사인 유사도 계산
    private double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // 기존 메서드들 유지
    @Transactional
    public Mono<KnowledgeBaseResponse> createOrUpdate(String topic, String content, List<String> keywords) {
        return knowledgeBaseRepository.findLatestByTopic(topic)
                .flatMap(existing -> {
                    KnowledgeBase newVersion = KnowledgeBase.builder()
                            .topic(topic)
                            .content(content)
                            .keywords(keywords)
                            .version(existing.getVersion() + 1)
                            .build();
                    return knowledgeBaseRepository.save(newVersion);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    KnowledgeBase newKb = KnowledgeBase.builder()
                            .topic(topic)
                            .content(content)
                            .keywords(keywords)
                            .version(1)
                            .build();
                    return knowledgeBaseRepository.save(newKb);
                }))
                .map(KnowledgeBaseResponse::from);
    }

    public Mono<List<KnowledgeVectorSearchResponse>> vectorSearchWithScore(String question, int topN) {
        return Mono.fromCallable(() ->
                        embeddingService.embed(question)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(questionVector ->
                        knowledgeBaseRepository.findAll()
                                .filter(kb -> kb.getEmbedding() != null && !kb.getEmbedding().isEmpty())
                                .collectList()
                                .map(all -> all.stream()
                                        .collect(Collectors.toMap(
                                                KnowledgeBase::getTopic,
                                                Function.identity(),
                                                (a, b) -> a.getVersion() >= b.getVersion() ? a : b
                                        ))
                                        .values()
                                        .stream()
                                        .map(kb -> new KnowledgeVectorSearchResponse(
                                                kb.getId(),
                                                kb.getTopic(),
                                                kb.getContent(),
                                                kb.getKeywords(),
                                                kb.getVersion(),
                                                cosineSimilarity(kb.getEmbedding(), questionVector)
                                        ))
                                        .sorted((a, b) -> Double.compare(b.similarity(), a.similarity()))
                                        .limit(topN)
                                        .toList()
                                )
                );
    }

    public Flux<KnowledgeBaseResponse> textSearch(String searchText) {
        return knowledgeBaseRepository.textSearch(searchText)
                .map(KnowledgeBaseResponse::from);
    }

    public Flux<KnowledgeBaseResponse> searchByKeywords(List<String> keywords) {
        return knowledgeBaseRepository.searchByKeywords(keywords)
                .map(KnowledgeBaseResponse::from);
    }

    public Mono<KnowledgeBaseResponse> findLatestByTopic(String topic) {
        return knowledgeBaseRepository.findLatestByTopic(topic)
                .map(KnowledgeBaseResponse::from);
    }

    public Flux<KnowledgeBaseResponse> findAll() {
        return knowledgeBaseRepository.findAll()
                .map(KnowledgeBaseResponse::from);
    }
}