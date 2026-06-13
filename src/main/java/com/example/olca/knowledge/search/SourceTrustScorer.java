package com.example.olca.knowledge.search;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SourceTrustScorer implements KnowledgeCandidateScorer {

    private static final double OFFICIAL_SOURCE_WEIGHT = 0.12;

    @Override
    public CandidateScoreContribution score(KnowledgeCandidateContext context) {
        String text = context.normalizedKnowledgeText();
        boolean officialSource = text.contains("official")
                || text.contains("oracle")
                || text.contains("docs.oracle.com")
                || text.contains("공식");

        if (!officialSource) {
            return CandidateScoreContribution.empty();
        }

        return new CandidateScoreContribution(
                0.0,
                0.0,
                OFFICIAL_SOURCE_WEIGHT,
                0.0,
                List.of("source_trust:official")
        );
    }
}
