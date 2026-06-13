package com.example.olca.knowledge.search;

import com.example.olca.knowledge.domain.KnowledgeBase;

import java.util.List;

public record KnowledgeCandidateContext(
        String question,
        List<String> queryKeywords,
        KnowledgeBase knowledgeBase,
        double vectorScore,
        List<String> matchedKeywords,
        String normalizedKnowledgeText
) {
}
