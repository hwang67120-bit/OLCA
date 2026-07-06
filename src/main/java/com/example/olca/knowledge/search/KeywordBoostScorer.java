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
        List<String> intentKeywords = KnowledgeSearchText.intentKeywords(context.matchedKeywords());
        if (intentKeywords.isEmpty()) {
            return CandidateScoreContribution.empty();
        }

        String normalizedTopic = KnowledgeSearchText.normalize(context.knowledgeBase().getTopic());
        long topicMatches = intentKeywords.stream()
                .filter(normalizedTopic::contains)
                .count();

        double keywordScore = intentKeywords.size() * MATCHED_KEYWORD_WEIGHT;
        double topicScore = topicMatches * TOPIC_KEYWORD_WEIGHT;

        List<String> reasons = new ArrayList<>();
        reasons.add("keyword_match:" + intentKeywords);
        if (topicMatches > 0) {
            reasons.add("topic_keyword_match:" + topicMatches);
        }

        return new CandidateScoreContribution(keywordScore, topicScore, 0.0, 0.0, reasons);
    }
}
