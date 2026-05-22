package com.example.olca.controller;



import com.example.olca.dto.request.ChatMessgeCreateRequset;
import com.example.olca.dto.response.ChatMessageResponse;
import com.example.olca.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // 대화 저장
    @PostMapping
    public ResponseEntity<ChatMessageResponse> create(
            @Valid @RequestBody ChatMessgeCreateRequset request
    ) {
        ChatMessageResponse response = chatMessageService.create(request);
        return ResponseEntity.ok(response);
    }

    // ID로 조회
    @GetMapping("/{id}")
    public ResponseEntity<ChatMessageResponse> findById(
            @PathVariable Long id
    ) {
        ChatMessageResponse response = chatMessageService.findById(id);
        return ResponseEntity.ok(response);
    }

    // 세션별 대화 목록 조회
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ChatMessageResponse>> findBySessionId(
            @PathVariable Long sessionId
    ) {
        List<ChatMessageResponse> responses = chatMessageService.findBySessionId(sessionId);
        return ResponseEntity.ok(responses);
    }
}