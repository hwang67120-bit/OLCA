package com.example.olca.knowledge.search;

import com.example.olca.knowledge.domain.KnowledgeBase;

import java.util.List;

public record KnowledgeSearchCandidate(
        KnowledgeBase knowledgeBase,
        double vectorScore,
        List<String> matchedKeywords,
        boolean topicPass,
        double keywordScore,
        double topicScore,
        double sourceTrustScore,
        double domainPenalty,
        double finalScore,
        List<String> reasons
) {
    public String topic() {
        return knowledgeBase.getTopic();
    }
}
