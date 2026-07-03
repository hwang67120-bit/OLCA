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

    @Override
    public CandidateScoreContribution score(KnowledgeCandidateContext context) {
        String question = KnowledgeSearchText.normalize(context.question());
        String topic = KnowledgeSearchText.normalize(context.knowledgeBase().getTopic());

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

    private boolean containsAny(String text, Set<String> terms) {
        return terms.stream().anyMatch(text::contains);
    }
}
