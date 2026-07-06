package com.example.olca.tag.contoller;


import com.example.olca.tag.dto.TagResponse;
import com.example.olca.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 저장된 태그를 입력 조건 없이 조회하고 전체 목록으로 출력한다.
     */
    @GetMapping
    public ResponseEntity<List<TagResponse>> findAll() {
        List<TagResponse> responses = tagService.findAll();
        return ResponseEntity.ok(responses);
    }

    /**
     * 입력된 태그 이름으로 단일 태그를 조회하고 응답으로 출력한다.
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<TagResponse> findByName(@PathVariable String name) {
        TagResponse response = tagService.findByName(name);
        return ResponseEntity.ok(response);
    }

    /**
     * 입력된 limit 기준으로 많이 사용된 태그를 계산하고 상위 목록을 출력한다.
     */
    @GetMapping("/top")
    public ResponseEntity<List<TagResponse>> getTopTags(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<TagResponse> responses = tagService.getTopTag(limit);
        return ResponseEntity.ok(responses);
    }
}
