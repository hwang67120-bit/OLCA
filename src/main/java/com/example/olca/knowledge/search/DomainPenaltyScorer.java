package com.example.olca.knowledge.search;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class DomainPenaltyScorer implements KnowledgeCandidateScorer {

    private static final double DESIGN_PATTERN_MISMATCH_PENALTY = 0.35;
    private static final Set<String> DESIGN_PATTERN_TERMS = Set.of(
            "design-pattern", "design pattern", "pattern", "패턴",
            "builder", "factory", "singleton", "빌더", "팩토리", "싱글톤"
    );
    private static final Set<String> JAVA_BASIC_TERMS = Set.of(
            "class", "classes", "object", "objects", "interface", "inheritance",
            "클래스", "객체", "인터페이스", "상속"
    );

    @Override
    public CandidateScoreContribution score(KnowledgeCandidateContext context) {
        String question = KnowledgeSearchText.normalize(context.question());
        String document = context.normalizedKnowledgeText();

        boolean documentIsDesignPattern = containsAny(document, DESIGN_PATTERN_TERMS);
        boolean questionWantsDesignPattern = containsAny(question, DESIGN_PATTERN_TERMS);
        boolean questionWantsJavaBasic = containsAny(question, JAVA_BASIC_TERMS);

        if (!documentIsDesignPattern || questionWantsDesignPattern || !questionWantsJavaBasic) {
            return CandidateScoreContribution.empty();
        }

        return new CandidateScoreContribution(
                0.0,
                0.0,
                0.0,
                DESIGN_PATTERN_MISMATCH_PENALTY,
                List.of("domain_penalty:design_pattern_mismatch")
        );
    }

    private boolean containsAny(String text, Set<String> terms) {
        return terms.stream().anyMatch(text::contains);
    }
}
