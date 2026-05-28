package com.example.olca.ai.controller;


import com.example.olca.ai.dto.PromptContext;
import com.example.olca.ai.promptBuilder.PromptBuilder;
import com.example.olca.ai.service.OllamaService;
import com.example.olca.ai.websocket.VTuberWebSocketClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final VTuberWebSocketClient vtuberClient;
    private final OllamaService ollamaService;
    private final PromptBuilder promptBuilder;

    @GetMapping("/websocket")
    public Mono<String> testWebSocket(@RequestParam String message) {
        return vtuberClient.sendMessage(message);
    }

    @GetMapping("/ollama")
    public Mono<String> testOllama(@RequestParam String message) {
        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(
                new PromptContext(message, List.of(), List.of(), List.of())
        );
        return Mono.fromCallable(() -> ollamaService.chat(systemPrompt, userPrompt))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/tts")
    public Mono<String> testTts(@RequestParam String message) {
        // Ollama 응답 → 엘리나 TTS
        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(
                new PromptContext(message, List.of(), List.of(), List.of())
        );
        return Mono.fromCallable(() -> ollamaService.chat(systemPrompt, userPrompt))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(response -> {
                    // TTS-only로 엘리나에 전달
                    String ttsMessage = String.format(
                            "{\"type\":\"tts-only\",\"text\":\"%s\"}",
                            response.replace("\"", "\\\"")
                    );
                    return vtuberClient.sendMessage(ttsMessage);
                });
    }

    @PostMapping("/chat")
    public Mono<String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(
                new PromptContext(message, List.of(), List.of(), List.of())
        );
        return Mono.fromCallable(() -> ollamaService.chat(systemPrompt, userPrompt))
                .subscribeOn(Schedulers.boundedElastic());
    }

}