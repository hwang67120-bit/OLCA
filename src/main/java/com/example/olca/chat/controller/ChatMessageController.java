package com.example.olca.chat.controller;


import com.example.olca.chat.dto.requst.ChatMessgeCreateRequset;
import com.example.olca.chat.dto.response.ChatMessageResponse;
import com.example.olca.chat.service.ChatMessageService;
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

    /**
     * 입력된 대화 저장 요청을 검증한 뒤 메시지로 저장하고 저장 결과를 출력한다.
     */
    @PostMapping
    public ResponseEntity<ChatMessageResponse> create(
            @Valid @RequestBody ChatMessgeCreateRequset request
    ) {
        ChatMessageResponse response = chatMessageService.create(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 입력된 메시지 ID로 대화를 조회하고 단일 응답으로 출력한다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChatMessageResponse> findById(
            @PathVariable Long id
    ) {
        ChatMessageResponse response = chatMessageService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 입력된 세션 ID로 대화 목록을 조회하고 시간 흐름에 맞는 목록으로 출력한다.
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ChatMessageResponse>> findBySessionId(
            @PathVariable Long sessionId
    ) {
        List<ChatMessageResponse> responses = chatMessageService.findBySessionId(sessionId);
        return ResponseEntity.ok(responses);
    }
}
