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

    /**
     * 저장된 지식 데이터를 입력 조건 없이 조회하고 전체 목록으로 출력한다.
     */
    @GetMapping
    public Flux<KnowledgeBaseResponse> findAll() {
        return knowledgeBaseService.findAll();
    }

    /**
     * 입력된 자연어 검색어를 텍스트 검색으로 처리하고 관련 지식 목록을 출력한다.
     */
    @GetMapping("/search")
    public Flux<KnowledgeBaseResponse> textSearch(@RequestParam String q) {
        return knowledgeBaseService.textSearch(q);
    }

    /**
     * 입력된 키워드 목록을 기준으로 지식을 필터링하고 일치 결과를 출력한다.
     */
    @GetMapping("/keywords")
    public Flux<KnowledgeBaseResponse> searchByKeywords(@RequestParam List<String> keywords) {
        return knowledgeBaseService.searchByKeywords(keywords);
    }

    /**
     * 입력된 topic의 최신 버전을 선택해 단일 지식 응답으로 출력한다.
     */
    @GetMapping("/topic/{topic}")
    public Mono<KnowledgeBaseResponse> findLatestByTopic(@PathVariable String topic) {
        return knowledgeBaseService.findLatestByTopic(topic);
    }

    @PostMapping
    public Mono<KnowledgeBaseResponse> save(@RequestBody KnowledgeBaseSaveRequest request) {

        return knowledgeBaseService.saveWithEmbedding(
                request.topic(),
                request.content(),
                request.keywords(),
                request.metadata()
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
