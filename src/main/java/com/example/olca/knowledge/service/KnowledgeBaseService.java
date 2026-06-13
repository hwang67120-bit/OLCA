package com.example.olca.knowledge.service;

import com.example.olca.ai.service.EmbeddingService;
import com.example.olca.global.trace.TraceLog;
import com.example.olca.knowledge.domain.KnowledgeBase;
import com.example.olca.knowledge.dto.response.KnowledgeVectorSearchResponse;
import com.example.olca.knowledge.repository.KnowledgeBaseRepository;
import com.example.olca.knowledge.search.KnowledgeReranker;
import com.example.olca.knowledge.search.KnowledgeSearchCandidate;
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

    private static final double MIN_VECTOR_SIMILARITY = 0.85;

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final EmbeddingService embeddingService;
    private final QueryExpansionService queryExpansionService;
    private final KnowledgeReranker knowledgeReranker;

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

    @TraceLog("KnowledgeBaseService.vectorSearch")
    public Mono<List<KnowledgeBase>> vectorSearch(String question, int topN) {
        return Mono.fromCallable(() -> {
                    String expandedQuestion = queryExpansionService.expand(question);
                    log.info("[VECTOR_SEARCH] expandedQuestionLength={} topN={}", expandedQuestion.length(), topN);
                    return embeddingService.embed(expandedQuestion);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(questionVector ->
                        knowledgeBaseRepository.findAll()
                                .filter(kb -> kb.getEmbedding() != null && !kb.getEmbedding().isEmpty())
                                .collectList()
                                .map(all -> {
                                    List<KnowledgeBase> latestDocuments = all.stream()
                                            .collect(Collectors.toMap(
                                                    KnowledgeBase::getTopic,
                                                    Function.identity(),
                                                    (a, b) -> a.getVersion() >= b.getVersion() ? a : b
                                            ))
                                            .values()
                                            .stream()
                                            .toList();

                                    return knowledgeReranker.rank(question, questionVector, latestDocuments, topN)
                                            .stream()
                                            .map(KnowledgeSearchCandidate::knowledgeBase)
                                            .toList();
                                })
                )
                .doOnSuccess(results ->
                        log.info("[VECTOR_SEARCH] minSimilarity={} resultCount={} topics={}",
                                MIN_VECTOR_SIMILARITY,
                                results.size(),
                                results.stream().map(KnowledgeBase::getTopic).toList())
                );
    }


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

    @TraceLog("KnowledgeBaseService.vectorSearchWithScore")
    public Mono<List<KnowledgeVectorSearchResponse>> vectorSearchWithScore(String question, int topN) {
        return Mono.fromCallable(() -> {
                    String expandedQuestion = queryExpansionService.expand(question);
                    log.info("[VECTOR_SEARCH_DEBUG] expandedQuestionLength={} topN={}", expandedQuestion.length(), topN);
                    return embeddingService.embed(expandedQuestion);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(questionVector ->
                        knowledgeBaseRepository.findAll()
                                .filter(kb -> kb.getEmbedding() != null && !kb.getEmbedding().isEmpty())
                                .collectList()
                                .map(all -> {
                                    List<KnowledgeBase> latestDocuments = all.stream()
                                            .collect(Collectors.toMap(
                                                    KnowledgeBase::getTopic,
                                                    Function.identity(),
                                                    (a, b) -> a.getVersion() >= b.getVersion() ? a : b
                                            ))
                                            .values()
                                            .stream()
                                            .toList();

                                    return knowledgeReranker.rank(question, questionVector, latestDocuments, topN)
                                            .stream()
                                            .map(candidate -> new KnowledgeVectorSearchResponse(
                                                    candidate.knowledgeBase().getId(),
                                                    candidate.knowledgeBase().getTopic(),
                                                    candidate.knowledgeBase().getContent(),
                                                    candidate.knowledgeBase().getKeywords(),
                                                    candidate.knowledgeBase().getVersion(),
                                                    candidate.vectorScore(),
                                                    candidate.finalScore(),
                                                    candidate.matchedKeywords(),
                                                    candidate.reasons()
                                            ))
                                            .toList();
                                })
                )
                .doOnSuccess(results ->
                        log.info("[VECTOR_SEARCH_DEBUG] resultCount={} topics={}",
                                results.size(),
                                results.stream().map(KnowledgeVectorSearchResponse::topic).toList())
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
