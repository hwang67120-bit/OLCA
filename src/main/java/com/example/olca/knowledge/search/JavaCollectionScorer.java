package com.example.olca.knowledge.search;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class JavaCollectionScorer implements KnowledgeCandidateScorer {

    private static final double COLLECTION_TOPIC_WEIGHT = 0.45;
    private static final Set<String> LIST_TERMS = Set.of(
            "list", "리스트", "순서", "인덱스", "중복 허용", "arraylist", "linkedlist"
    );
    private static final Set<String> SET_TERMS = Set.of(
            "set", "집합", "중복 제거", "유일", "unique", "hashset", "treeset", "linkedhashset"
    );
    private static final Set<String> MAP_TERMS = Set.of(
            "map", "맵", "key", "value", "키", "값", "hashmap", "treemap", "linkedhashmap"
    );
    private static final Set<String> COLLECTION_TERMS = Set.of(
            "collection", "collections", "컬렉션", "iterator", "순회", "add", "remove", "contains"
    );
    private static final Set<String> GENERICS_CONTEXT_TERMS = Set.of(
            "generics", "generic", "제네릭", "타입 파라미터", "타입파라미터", "타입 안정성", "타입안정성"
    );
    private static final Set<String> STREAM_OPERATION_TERMS = Set.of(
            "stream", "streams", "스트림", "filter", "map", "collect", "pipeline", "파이프라인", "중간 연산", "최종 연산", "중간연산", "최종연산"
    );

    @Override
    public CandidateScoreContribution score(KnowledgeCandidateContext context) {
        String question = KnowledgeSearchText.normalize(context.question());
        String topic = KnowledgeSearchText.normalize(context.knowledgeBase().getTopic());

        if (isGenericTypeQuestion(question) || isStreamOperationQuestion(question)) {
            return CandidateScoreContribution.empty();
        }

        if (matches(question, topic, LIST_TERMS, "list interface")) {
            return boost("java_collection:list");
        }
        if (matches(question, topic, SET_TERMS, "set interface")) {
            return boost("java_collection:set");
        }
        if (matches(question, topic, MAP_TERMS, "map interface")) {
            return boost("java_collection:map");
        }
        if (matches(question, topic, COLLECTION_TERMS, "collection interface")) {
            return boost("java_collection:collection");
        }

        return CandidateScoreContribution.empty();
    }

    private boolean isGenericTypeQuestion(String question) {
        return containsAny(question, GENERICS_CONTEXT_TERMS)
                || question.contains("<")
                || (question.contains("타입") && containsAny(question, Set.of("붙이는", "붙이", "명시")));
    }

    private boolean isStreamOperationQuestion(String question) {
        boolean hasExplicitStream = containsAny(question, Set.of("stream", "스트림", "pipeline", "파이프라인", "중간", "최종"));
        boolean hasStreamOperationToken = hasToken(question, "filter") || hasToken(question, "collect")
                || (hasToken(question, "map") && (hasToken(question, "filter") || hasToken(question, "collect")));

        return hasExplicitStream || hasStreamOperationToken;
    }

    private boolean matches(String question, String topic, Set<String> terms, String topicMarker) {
        return containsAny(question, terms) && topic.contains(topicMarker);
    }

    private CandidateScoreContribution boost(String reason) {
        return new CandidateScoreContribution(
                0.0,
                COLLECTION_TOPIC_WEIGHT,
                0.0,
                0.0,
                List.of(reason)
        );
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
