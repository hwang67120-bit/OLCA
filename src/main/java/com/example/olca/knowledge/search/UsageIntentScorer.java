package com.example.olca.knowledge.search;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UsageIntentScorer implements KnowledgeCandidateScorer {

    private static final double USAGE_TOPIC_WEIGHT = 0.35;
    private static final Set<String> USAGE_INTENT_TERMS = Set.of(
            "언제", "사용 시점", "사용시점", "쓸 때", "써", "쓰는", "쓰면", "좋아", "적합"
    );

    @Override
    public CandidateScoreContribution score(KnowledgeCandidateContext context) {
        String question = KnowledgeSearchText.normalize(context.question());
        String topic = KnowledgeSearchText.normalize(context.knowledgeBase().getTopic());

        /**
         * Prefer usage-focused documents for usage-intent questions.
         * Input: user question and candidate topic.
         * Process: detect when/usage wording and check whether the topic is a usage document.
         * Output: "사용 시점" documents outrank generic concept summaries.
         */
        if (!containsAny(question, USAGE_INTENT_TERMS) || !topic.contains("사용 시점")) {
            return CandidateScoreContribution.empty();
        }

        return new CandidateScoreContribution(
                0.0,
                USAGE_TOPIC_WEIGHT,
                0.0,
                0.0,
                List.of("usage_intent_boost")
        );
    }

    private boolean containsAny(String text, Set<String> terms) {
        return terms.stream().anyMatch(text::contains);
    }
}