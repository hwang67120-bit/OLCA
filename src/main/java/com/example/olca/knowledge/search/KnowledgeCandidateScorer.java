package com.example.olca.knowledge.search;

public interface KnowledgeCandidateScorer {

    CandidateScoreContribution score(KnowledgeCandidateContext context);
}
