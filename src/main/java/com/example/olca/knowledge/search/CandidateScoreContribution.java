package com.example.olca.knowledge.search;

import java.util.List;

public record CandidateScoreContribution(
        double keywordScore,
        double topicScore,
        double sourceTrustScore,
        double domainPenalty,
        List<String> reasons
) {
    public static CandidateScoreContribution empty() {
        return new CandidateScoreContribution(0.0, 0.0, 0.0, 0.0, List.of());
    }
}
