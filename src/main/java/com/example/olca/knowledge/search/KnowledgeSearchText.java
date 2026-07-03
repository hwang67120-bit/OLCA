package com.example.olca.knowledge.search;

import com.example.olca.knowledge.domain.KnowledgeBase;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class KnowledgeSearchText {

    private static final Set<String> STOP_WORDS = Set.of(
            "궁금", "질문", "검색", "찾아줘", "찾아", "알려줘", "알려", "설명", "정리",
            "무엇", "뭐야", "왜", "어떻게", "언제", "차이", "비교", "사용법",
            "자료", "내용", "자세히", "간단히",
            "the", "and", "for", "with", "about"
    );
    private static final List<String> KOREAN_PARTICLES = List.of(
            "이랑", "하고", "에서", "으로", "에게", "한테",
            "랑", "로", "은", "는", "이", "가", "을", "를", "와", "과", "도"
    );

    private KnowledgeSearchText() {
    }

    public static String normalize(String text) {
        String normalized = (text == null ? "" : text).toLowerCase(Locale.ROOT);

        return normalized
                .replace("자바", "자바 java")
                .replace("클래스", "클래스 class classes")
                .replace("객체", "객체 object objects")
                .replace("인터페이스", "인터페이스 interface")
                .replace("상속", "상속 inheritance")
                .replace("빌더", "빌더 builder")
                .replace("팩토리", "팩토리 factory")
                .replace("싱글톤", "싱글톤 singleton")
                .replace("컬렉션", "컬렉션 collection collections")
                .replace("리스트", "리스트 list")
                .replace("맵", "맵 map")
                .replace("중복 제거", "중복 제거 set unique")
                .replace("예외처리", "예외처리 exception exceptions try catch throw throws")
                .replace("예외", "예외 exception exceptions try catch throw throws")
                .replace("throws", "throws throw exception exceptions 예외")
                .replace("throw", "throw exception exceptions 예외")
                .replace("try", "try exception exceptions 예외")
                .replace("catch", "catch exception exceptions 예외")
                .replace("스트림", "스트림 stream");
    }

    public static List<String> extractKeywords(String question) {
        String normalizedQuestion = normalize(question);

        return Arrays.stream(normalizedQuestion.split("[^a-z0-9가-힣]+"))
                .map(String::trim)
                .flatMap(word -> keywordVariants(word).stream())
                .filter(word -> word.length() >= 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .distinct()
                .toList();
    }

    private static List<String> keywordVariants(String word) {
        String stripped = stripKoreanParticle(word);

        if (stripped.equals(word)) {
            return List.of(word);
        }

        return List.of(word, stripped);
    }

    private static String stripKoreanParticle(String word) {
        if (word.length() <= 2) {
            return word;
        }

        for (String particle : KOREAN_PARTICLES) {
            int stemLength = word.length() - particle.length();
            if (word.endsWith(particle) && stemLength >= 2) {
                return word.substring(0, stemLength);
            }
        }

        return word;
    }

    public static String documentText(KnowledgeBase knowledgeBase) {
        return normalize(
                knowledgeBase.getTopic() + " "
                        + String.join(" ", knowledgeBase.getKeywords() == null ? List.of() : knowledgeBase.getKeywords())
                        + " "
                        + knowledgeBase.getContent()
        );
    }

    public static List<String> matchedKeywords(List<String> queryKeywords, String normalizedDocumentText) {
        if (queryKeywords.isEmpty()) {
            return List.of();
        }

        return queryKeywords.stream()
                .filter(normalizedDocumentText::contains)
                .distinct()
                .toList();
    }
}
