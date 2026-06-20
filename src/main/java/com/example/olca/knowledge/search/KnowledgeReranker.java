package com.example.olca.knowledge.search;

import com.example.olca.knowledge.domain.KnowledgeBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeReranker {

    private static final double MIN_VECTOR_SIMILARITY = 0.85;
    private static final double MIN_FINAL_SCORE = 0.70;
    private final List<KnowledgeCandidateScorer> scorers;

    public List<KnowledgeSearchCandidate> rank(
            String question,
            List<Double> questionVector,
            List<KnowledgeBase> documents,
            int topN
    ) {
        List<String> queryKeywords = KnowledgeSearchText.extractKeywords(question);
        log.info("[TOPIC_QUERY] keywords={}(질문핵심키워드)", queryKeywords);

        return documents.stream()
                .map(document -> score(question, queryKeywords, questionVector, document))
                .peek(this::logScore)
                .filter(this::usable)
                .sorted(Comparator.comparingDouble(KnowledgeSearchCandidate::finalScore).reversed())
                .limit(topN)
                .toList();
    }

    private KnowledgeSearchCandidate score(
            String question,
            List<String> queryKeywords,
            List<Double> questionVector,
            KnowledgeBase document
    ) {
        double vectorScore = cosineSimilarity(document.getEmbedding(), questionVector);
        String normalizedDocumentText = KnowledgeSearchText.documentText(document);
        List<String> matchedKeywords = KnowledgeSearchText.matchedKeywords(queryKeywords, normalizedDocumentText);
        boolean topicPass = !matchedKeywords.isEmpty();

        KnowledgeCandidateContext context = new KnowledgeCandidateContext(
                question,
                queryKeywords,
                document,
                vectorScore,
                matchedKeywords,
                normalizedDocumentText
        );

        double keywordScore = 0.0;
        double topicScore = topicPass ? 0.10 : 0.0;
        double sourceTrustScore = 0.0;
        double domainPenalty = 0.0;
        List<String> reasons = new ArrayList<>();

        if (topicPass) {
            reasons.add("topic_pass");
        }

        for (KnowledgeCandidateScorer scorer : scorers) {
            CandidateScoreContribution contribution = scorer.score(context);
            keywordScore += contribution.keywordScore();
            topicScore += contribution.topicScore();
            sourceTrustScore += contribution.sourceTrustScore();
            domainPenalty += contribution.domainPenalty();
            reasons.addAll(contribution.reasons());
        }

        double finalScore = vectorScore + keywordScore + topicScore + sourceTrustScore - domainPenalty;

        return new KnowledgeSearchCandidate(
                document,
                vectorScore,
                matchedKeywords,
                topicPass,
                keywordScore,
                topicScore,
                sourceTrustScore,
                domainPenalty,
                finalScore,
                reasons
        );
    }

    private boolean usable(KnowledgeSearchCandidate candidate) {
        return candidate.topicPass()
                && candidate.finalScore() >= MIN_FINAL_SCORE;
    }

    private void logScore(KnowledgeSearchCandidate candidate) {
        log.info(
                "[RAG_SCORE] topic={} vector={} keyword={} topicScore={} sourceTrust={} penalty={} final={} usable={} matchedKeywords={} reasons={}",
                candidate.topic(),
                String.format("%.4f", candidate.vectorScore()),
                String.format("%.4f", candidate.keywordScore()),
                String.format("%.4f", candidate.topicScore()),
                String.format("%.4f", candidate.sourceTrustScore()),
                String.format("%.4f", candidate.domainPenalty()),
                String.format("%.4f", candidate.finalScore()),
                usable(candidate),
                candidate.matchedKeywords(),
                candidate.reasons()
        );
    }

    private double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
