package com.example.olca.global.config;

import com.example.olca.ai.dto.Chatstsettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatConfig {

    @Bean
    public Chatstsettings Chatstsettings() {
        return Chatstsettings.builder()
                .maxPastMessages(10)
                .maxKnowledgeResults(1)
                .minRelevanceScore(0.6)
                .build();
    }

    @Bean
    public List<String> stopWords(){
        return List.of(
                "은", "는", "이", "가", "을", "를",
                "에", "의", "와", "과", "도", "만",
                "라", "이랑", "하고", "어", "아", "요"
        );
    }
}
