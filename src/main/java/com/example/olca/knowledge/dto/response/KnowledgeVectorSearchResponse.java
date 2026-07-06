package com.example.olca.knowledge.dto.response;

import com.example.olca.knowledge.domain.KnowledgeMetadata;

import java.util.List;

public record KnowledgeVectorSearchResponse(
        String id,
        String topic,
        String content,
        List<String> keywords,
        KnowledgeMetadata metadata,
        Integer version,
        double similarity,
        double finalScore,
        List<String> matchedKeywords,
        List<String> reasons
) {
}
