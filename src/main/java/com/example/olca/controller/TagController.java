package com.example.olca.controller;


import com.example.olca.dto.request.TagResponse;
import com.example.olca.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // 전체 태그 조회
    @GetMapping
    public ResponseEntity<List<TagResponse>> findAll() {
        List<TagResponse> responses = tagService.findAll();
        return ResponseEntity.ok(responses);
    }

    // 이름으로 태그 조회
    @GetMapping("/name/{name}")
    public ResponseEntity<TagResponse> findByName(@PathVariable String name) {
        TagResponse response = tagService.findByName(name);
        return ResponseEntity.ok(response);
    }

    // 상위 N개 태그 조회 (많이 쓴 순)
    @GetMapping("/top")
    public ResponseEntity<List<TagResponse>> getTopTags(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<TagResponse> responses = tagService.getTopTag(limit);
        return ResponseEntity.ok(responses);
    }
}