package com.example.olca.knowledge.domain;

import lombok.Builder;

@Builder
public record KnowledgeMetadata(
        String sourceType,
        String sourceName,
        String sourceUrl,
        String domain,
        String category,
        String topicKey
) {
    public static KnowledgeMetadata empty() {
        return new KnowledgeMetadata(null, null, null, null, null, null);
    }
}