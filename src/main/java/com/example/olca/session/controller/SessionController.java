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

    /**
     * 입력된 세션 생성 요청을 검증한 뒤 새 채팅방을 만들고 생성 결과를 출력한다.
     */
    @PostMapping
    public ResponseEntity<SessionResponse> create(
            @Valid @RequestBody SessionCreateRequest request
    ) {
        SessionResponse response = sessionService.create(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 입력된 사용자 ID로 소유 세션을 필터링하고 목록 응답으로 출력한다.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionResponse>> findByUserId(
            @PathVariable Long userId
    ) {
        List<SessionResponse> responses = sessionService.findByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 입력된 세션 ID로 단일 채팅방을 조회하고 상세 응답으로 출력한다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> findById(
            @PathVariable Long id
    ) {
        SessionResponse response = sessionService.findById(id);
        return ResponseEntity.ok(response);
    }
}
