package com.example.olca.ai.service;

import com.example.olca.global.trace.TraceLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OllamaService {

    private final ChatClient client;

    public OllamaService(ChatClient.Builder builder) {
        this.client = builder.build();
    }

    @TraceLog("OllamaService.chat")
    public String chat(String sytemPrompt, String userMessage) {
        log.info("[LLM_REQUEST] systemPromptLength={} userPromptLength={}",
                sytemPrompt == null ? 0 : sytemPrompt.length(),
                userMessage == null ? 0 : userMessage.length());

        String response = client.prompt()
                .system(sytemPrompt)
                .user(userMessage)
                .call()
                .content();

        log.info("[LLM_RESPONSE] responseLength={}", response == null ? 0 : response.length());
        return response;
    }
}
