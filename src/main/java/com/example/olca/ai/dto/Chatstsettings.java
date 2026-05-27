package com.example.olca.ai.dto;

import lombok.Builder;

@Builder
public record Chatstsettings(
        int maxPastMessages,
        int minWordLength,
        int maxKnowledgeResults,
        double minRelevanceScore
) {
}
