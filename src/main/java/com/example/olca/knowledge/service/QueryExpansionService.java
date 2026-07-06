package com.example.olca.knowledge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueryExpansionService {

    @Value("${olca.rag.query-expansion.enabled:true}")
    private boolean enabled;

    public String expand(String question) {
        if (!enabled) {
            return question;
        }

        StringBuilder expanded = new StringBuilder(question);

        if (containsAny(question, "빌더", "생성자", "파라미터", "선택 값", "선택값", "오버로딩")) {
            expanded.append(" builder pattern many constructor parameters optional fields object construction step by step construction fluent api");
        }

        if (containsAny(question, "인터페이스", "구현체", "구현 클래스", "계약", "api 계약", "implements")) {
            expanded.append(" java official interface interfaces implementation implements contract abstraction type");
        }

        if (containsAny(question, "팩토리", "factory", "생성 책임", "생성책임", "객체 생성 책임")
                || (containsAny(question, "구현체", "구체 클래스") && containsAny(question, "생성", "선택", "new"))) {
            expanded.append(" factory pattern concrete class implementation object creation responsibility hide concrete class choose implementation");
        }

        if (containsAny(question, "싱글톤", "하나만", "전역", "공유", "같은 인스턴스", "단일 인스턴스")) {
            expanded.append(" singleton pattern single instance global access shared instance application wide instance");
        }

        /**
         * Expand exception syntax questions before embedding.
         * Input: Java exception terms that may be attached to Korean particles.
         * Process: add official exception vocabulary for vector recall.
         * Output: exception documents compete with package/class documents on the right topic.
         */
        if (containsAny(question, "예외", "예외처리", "try", "catch", "throw", "throws")) {
            expanded.append(" java official exception exceptions try catch throw throws error handling checked exception runtime exception");
        }

        return expanded.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}