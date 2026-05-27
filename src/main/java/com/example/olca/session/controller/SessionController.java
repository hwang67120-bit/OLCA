package com.example.olca.session.controller;

import com.example.olca.session.dto.request.SessionCreateRequest;
import com.example.olca.session.repository.SessionResponse;
import com.example.olca.session.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // 새 채팅방 생성
    @PostMapping
    public ResponseEntity<SessionResponse> create(
            @Valid @RequestBody SessionCreateRequest request
    ) {
        SessionResponse response = sessionService.create(request);
        return ResponseEntity.ok(response);
    }

    // 사용자별 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionResponse>> findByUserId(
            @PathVariable Long userId
    ) {
        List<SessionResponse> responses = sessionService.findByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    // ID로 조회
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> findById(
            @PathVariable Long id
    ) {
        SessionResponse response = sessionService.findById(id);
        return ResponseEntity.ok(response);
    }
}