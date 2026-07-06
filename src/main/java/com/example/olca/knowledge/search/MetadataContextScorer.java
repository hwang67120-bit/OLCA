package com.example.olca.knowledge.search;

import com.example.olca.knowledge.domain.KnowledgeMetadata;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class MetadataContextScorer implements KnowledgeCandidateScorer {

    private static final double METADATA_CONTEXT_WEIGHT = 0.30;
    private static final double METADATA_CONTEXT_PENALTY = 0.20;

    @Override
    public CandidateScoreContribution score(KnowledgeCandidateContext context) {
        KnowledgeMetadata metadata = context.knowledgeBase().getMetadata();
        if (metadata == null || metadata.topicKey() == null || metadata.topicKey().isBlank()) {
            return CandidateScoreContribution.empty();
        }

        String question = KnowledgeSearchText.normalize(context.question());
        if (isUnknownPatternQuestion(question)) {
            return CandidateScoreContribution.empty();
        }
        String category = normalize(metadata.category());
        String topicKey = normalize(metadata.topicKey());

        if (isGenericsQuestion(question)) {
            return scoreTopic(topicKey, "java-generics", category, "collection", "metadata_context:generics");
        }
        if (isStreamQuestion(question)) {
            return scoreTopic(topicKey, "java-stream-operations", category, "collection", "metadata_context:stream");
        }
        if (isCollectionQuestion(question)) {
            return scoreCollection(topicKey, category);
        }

        return CandidateScoreContribution.empty();
    }

    private CandidateScoreContribution scoreTopic(
            String topicKey,
            String expectedTopicKey,
            String category,
            String mismatchCategory,
            String reason
    ) {
        if (topicKey.equals(expectedTopicKey)) {
            return boost(reason);
        }
        if (category.equals(mismatchCategory)) {
            return penalty(reason + "_mismatch");
        }
        return CandidateScoreContribution.empty();
    }

    private CandidateScoreContribution scoreCollection(String topicKey, String category) {
        if (!category.equals("collection")) {
            return CandidateScoreContribution.empty();
        }
        if (containsAny(topicKey, Set.of("collection", "list", "set", "map"))) {
            return boost("metadata_context:collection");
        }
        return CandidateScoreContribution.empty();
    }

    private boolean isGenericsQuestion(String question) {
        return containsAny(question, Set.of("generics", "generic", "제네릭", "타입 파라미터", "타입파라미터", "타입 안정성", "타입안정성"))
                || question.contains("<")
                || (question.contains("타입") && containsAny(question, Set.of("붙이는", "붙이", "명시")));
    }

    private boolean isStreamQuestion(String question) {
        boolean hasExplicitStream = containsAny(question, Set.of("stream", "스트림", "pipeline", "파이프라인", "중간", "최종"));
        boolean hasStreamOperationToken = hasToken(question, "filter") || hasToken(question, "collect")
                || (hasToken(question, "map") && (hasToken(question, "filter") || hasToken(question, "collect")));
        return hasExplicitStream || hasStreamOperationToken;
    }

    private boolean isCollectionQuestion(String question) {
        return containsAny(question, Set.of(
                "collection", "collections", "컬렉션", "list", "리스트", "set", "집합", "map", "맵",
                "arraylist", "linkedlist", "hashset", "treeset", "linkedhashset", "hashmap", "treemap", "linkedhashmap",
                "key", "value", "키", "값", "중복", "순서", "인덱스"
        ));
    }

    private CandidateScoreContribution boost(String reason) {
        return new CandidateScoreContribution(0.0, METADATA_CONTEXT_WEIGHT, 0.0, 0.0, List.of(reason));
    }

    private CandidateScoreContribution penalty(String reason) {
        return new CandidateScoreContribution(0.0, 0.0, 0.0, METADATA_CONTEXT_PENALTY, List.of(reason));
    }

    private String normalize(String text) {
        return KnowledgeSearchText.normalize(text == null ? "" : text);
    }

    private boolean isUnknownPatternQuestion(String question) {
        boolean wantsPattern = containsAny(question, Set.of("pattern", "패턴"));
        boolean knownPattern = containsAny(question, Set.of("builder", "factory", "singleton", "빌더", "팩토리", "싱글톤"));
        return wantsPattern && !knownPattern;
    }

    private boolean hasToken(String text, String token) {
        for (String word : text.split("[^a-z0-9가-힣]+")) {
            if (word.equals(token)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAny(String text, Set<String> terms) {
        return terms.stream().anyMatch(text::contains);
    }
}