package com.example.olca.knowledge.dto.response;

import java.util.List;

public record KnowledgeVectorSearchResponse(
        String id,
        String topic,
        String content,
        List<String> keywords,
        Integer version,
        double similarity
) {
}