package com.example.olca.knowledge.service;

import com.example.olca.knowledge.domain.KnowledgeBase;
import com.example.olca.session.dto.response.KnowledgeBaseResponse;
import com.example.olca.knowledge.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    @Transactional
    public Mono<KnowledgeBaseResponse> createOrUpdate(String topic, String content, List<String> keywords) {
        return knowledgeBaseRepository.findLatestByTopic(topic)
                .flatMap(existing -> {
                    // 기존 존재 시 새 버전 생성
                    KnowledgeBase newVersion = KnowledgeBase.builder()
                            .topic(topic)
                            .content(content)
                            .keywords(keywords)
                            .version(existing.getVersion() + 1)
                            .build();
                    return knowledgeBaseRepository.save(newVersion);
                })
                .switchIfEmpty(
                        // 없으면 새로 생성
                        Mono.defer(() -> {
                            KnowledgeBase newKb = KnowledgeBase.builder()
                                    .topic(topic)
                                    .content(content)
                                    .keywords(keywords)
                                    .version(1)
                                    .build();
                            return knowledgeBaseRepository.save(newKb);
                        })
                )
                .map(KnowledgeBaseResponse::from);
    }

    public Flux<KnowledgeBaseResponse> textSearch(String searchTxet) {
        return knowledgeBaseRepository.textSearch(searchTxet)
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
