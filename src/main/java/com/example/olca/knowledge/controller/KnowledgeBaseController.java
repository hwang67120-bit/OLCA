package com.example.olca.knowledge.controller;


import com.example.olca.knowledge.dto.request.KnowledgeBaseSaveRequest;
import com.example.olca.knowledge.dto.request.KnowledgeImportRequest;
import com.example.olca.knowledge.dto.response.KnowledgeImportResponse;
import com.example.olca.knowledge.dto.response.KnowledgeVectorSearchResponse;
import com.example.olca.knowledge.service.KnowledgeBaseService;
import com.example.olca.knowledge.service.KnowledgeImportService;
import com.example.olca.session.dto.response.KnowledgeBaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeImportService knowledgeImportService;

    // 전체 조회
    @GetMapping
    public Flux<KnowledgeBaseResponse> findAll() {
        return knowledgeBaseService.findAll();
    }

    // 자연어 검색
    @GetMapping("/search")
    public Flux<KnowledgeBaseResponse> textSearch(@RequestParam String q) {
        return knowledgeBaseService.textSearch(q);
    }

    // Keywords 검색
    @GetMapping("/keywords")
    public Flux<KnowledgeBaseResponse> searchByKeywords(@RequestParam List<String> keywords) {
        return knowledgeBaseService.searchByKeywords(keywords);
    }

    // Topic 최신 버전 조회
    @GetMapping("/topic/{topic}")
    public Mono<KnowledgeBaseResponse> findLatestByTopic(@PathVariable String topic) {
        return knowledgeBaseService.findLatestByTopic(topic);
    }

    @PostMapping
    public Mono<KnowledgeBaseResponse> save(@RequestBody KnowledgeBaseSaveRequest request) {

        return knowledgeBaseService.saveWithEmbedding(
                request.topic(),
                request.content(),
                request.keywords()
        );
    }

    @GetMapping("/vector-search")
    public Mono<List<KnowledgeVectorSearchResponse>> vectorSearch(
            @RequestParam String question,
            @RequestParam(defaultValue = "5") int topN
    ) {
        return knowledgeBaseService.vectorSearchWithScore(question, topN);
    }

    @PostMapping("/import-text")
    public Mono<KnowledgeImportResponse> importText(
            @Valid @RequestBody KnowledgeImportRequest request
    ) {
        return knowledgeImportService.importText(request);
    }
}