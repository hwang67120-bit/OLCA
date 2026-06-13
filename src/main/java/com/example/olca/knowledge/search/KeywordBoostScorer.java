package com.example.olca.knowledge.search;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeywordBoostScorer implements KnowledgeCandidateScorer {

    private static final double MATCHED_KEYWORD_WEIGHT = 0.12;
    private static final double TOPIC_KEYWORD_WEIGHT = 0.08;

    @Override
    public CandidateScoreContribution score(KnowledgeCandidateContext context) {
        if (context.matchedKeywords().isEmpty()) {
            return CandidateScoreContribution.empty();
        }

        String normalizedTopic = KnowledgeSearchText.normalize(context.knowledgeBase().getTopic());
        long topicMatches = context.matchedKeywords().stream()
                .filter(normalizedTopic::contains)
                .count();

        double keywordScore = context.matchedKeywords().size() * MATCHED_KEYWORD_WEIGHT;
        double topicScore = topicMatches * TOPIC_KEYWORD_WEIGHT;

        List<String> reasons = new ArrayList<>();
        reasons.add("keyword_match:" + context.matchedKeywords());
        if (topicMatches > 0) {
            reasons.add("topic_keyword_match:" + topicMatches);
        }

        return new CandidateScoreContribution(keywordScore, topicScore, 0.0, 0.0, reasons);
    }
}
