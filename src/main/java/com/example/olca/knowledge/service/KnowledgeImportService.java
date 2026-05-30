package com.example.olca.knowledge.service;

import com.example.olca.knowledge.dto.request.KnowledgeImportRequest;
import com.example.olca.knowledge.dto.response.KnowledgeImportResponse;
import com.example.olca.knowledge.repository.KnowledgeBaseRepository;
import com.example.olca.session.dto.response.KnowledgeBaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class KnowledgeImportService {

    private final TextChunker textCriteria;
    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public Mono<KnowledgeImportResponse> importText(KnowledgeImportRequest request) {
        List<String> chunks = textCriteria.chunk(request.text());
        List<String> safekeywords = request.keywords() == null
                ? List.of()
                : request.keywords();

        AtomicInteger index = new AtomicInteger(1);

        return Flux.fromIterable(chunks)
                .concatMap(chunk -> {
                    int currentIndex = index.getAndIncrement();
                    String topic = request.sourceTitle() + " #" + currentIndex;

                    return knowledgeBaseService.saveWithEmbedding(
                            topic,
                            chunk,
                            safekeywords
                    );

                })
                .collectList()
                .map(saved -> new KnowledgeImportResponse(
                        request.sourceTitle(),
                        chunks.size(),
                        saved.size(),
                        saved.stream()
                                .map(KnowledgeBaseResponse::topic)
                                .toList()
                ));

    }
}
