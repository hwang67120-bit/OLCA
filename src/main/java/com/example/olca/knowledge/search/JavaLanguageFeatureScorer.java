package com.example.olca.knowledge.search;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class JavaLanguageFeatureScorer implements KnowledgeCandidateScorer {

    private static final double LANGUAGE_FEATURE_WEIGHT = 0.45;
    private static final Set<String> GENERICS_TERMS = Set.of(
            "generics", "generic", "제네릭", "타입 파라미터", "타입파라미터", "타입 안정성", "타입안정성"
    );
    private static final Set<String> ENUM_TERMS = Set.of(
            "enum", "enums", "열거형", "상수", "상태값", "상태 값"
    );
    private static final Set<String> ANNOTATION_TERMS = Set.of(
            "annotation", "annotations", "어노테이션", "애너테이션", "@override", "메타데이터"
    );
    private static final Set<String> LAMBDA_TERMS = Set.of(
            "lambda", "람다", "람다식", "함수형 인터페이스", "함수형인터페이스", "익명 클래스"
    );
    private static final Set<String> STREAM_TERMS = Set.of(
            "stream", "streams", "스트림", "filter", "map", "collect", "pipeline", "파이프라인", "중간 연산", "최종 연산", "중간연산", "최종연산"
    );

    @Override
    public CandidateScoreContribution score(KnowledgeCandidateContext context) {
        String question = KnowledgeSearchText.normalize(context.question());
        if (isUnknownPatternQuestion(question)) {
            return CandidateScoreContribution.empty();
        }
        String topic = KnowledgeSearchText.normalize(context.knowledgeBase().getTopic());

        if (matchesGenerics(question, topic)) {
            return boost("java_language_feature:generics");
        }
        if (matches(question, topic, ENUM_TERMS, "enum types")) {
            return boost("java_language_feature:enum");
        }
        if (matches(question, topic, ANNOTATION_TERMS, "annotations")) {
            return boost("java_language_feature:annotations");
        }
        if (matches(question, topic, LAMBDA_TERMS, "lambda expressions")) {
            return boost("java_language_feature:lambda");
        }
        if (matchesStream(question, topic)) {
            return boost("java_language_feature:stream");
        }

        return CandidateScoreContribution.empty();
    }

    private boolean matchesGenerics(String question, String topic) {
        return topic.contains("generics")
                && (containsAny(question, GENERICS_TERMS)
                || question.contains("<")
                || (question.contains("타입") && containsAny(question, Set.of("붙이는", "붙이", "명시"))));
    }

    private boolean matchesStream(String question, String topic) {
        if (!topic.contains("stream aggregate operations")) {
            return false;
        }

        boolean hasExplicitStream = containsAny(question, Set.of("stream", "스트림", "pipeline", "파이프라인", "중간", "최종"));
        boolean hasStreamOperationToken = hasToken(question, "filter") || hasToken(question, "collect")
                || (hasToken(question, "map") && (hasToken(question, "filter") || hasToken(question, "collect")));

        return hasExplicitStream || hasStreamOperationToken;
    }

    private boolean matches(String question, String topic, Set<String> terms, String topicMarker) {
        return topic.contains(topicMarker) && containsAny(question, terms);
    }

    private CandidateScoreContribution boost(String reason) {
        return new CandidateScoreContribution(
                0.0,
                LANGUAGE_FEATURE_WEIGHT,
                0.0,
                0.0,
                List.of(reason)
        );
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