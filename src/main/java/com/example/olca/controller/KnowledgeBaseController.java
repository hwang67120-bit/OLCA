package com.example.olca.controller;



import com.example.olca.dto.response.KnowledgeBaseResponse;
import com.example.olca.service.KnowledgeBaseService;
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
}