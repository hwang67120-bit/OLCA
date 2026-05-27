package com.example.olca.ai.controller;


import com.example.olca.ai.websocket.VTuberWebSocketClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final VTuberWebSocketClient vtuberClient;

    @GetMapping("/websocket")
    public Mono<String> testWebSocket(@RequestParam String message) {
        return vtuberClient.sendMessage(message);
    }
}