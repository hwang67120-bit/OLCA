package com.example.olca.ai.service;

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

    public String chat(String sytemPrompt, String userMessage) {
        log.info("Ollama 호출");
        log.info("질문: {}", userMessage);

        String response = client.prompt()
                .system(sytemPrompt)
                .user(userMessage)
                .call()
                .content();
        log.info("응답: {}",response);
        return response;
    }

}
