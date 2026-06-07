package com.example.olca.knowledge.service;

import org.springframework.stereotype.Service;

@Service
public class QueryExpansionService {

    public String expand(String question) {
        StringBuilder expanded = new StringBuilder(question);

        if (containsAny(question, "빌더", "생성자", "파라미터", "선택 값", "선택값", "오버로딩")) {
            expanded.append(" builder pattern many constructor parameters optional fields object construction step by step construction fluent api");
        }

        if (containsAny(question, "팩토리", "생성 책임", "생성책임", "구현체", "구체 클래스", "new", "객체 생성 책임")) {
            expanded.append(" factory pattern concrete class implementation object creation responsibility hide concrete class choose implementation");
        }

        if (containsAny(question, "싱글톤", "하나만", "전역", "공유", "같은 인스턴스", "단일 인스턴스")) {
            expanded.append(" singleton pattern single instance global access shared instance application wide instance");
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