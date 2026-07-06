package com.example.olca.knowledge.service;

import org.springframework.stereotype.Service;

@Service
public class KnowledgeDomainGuard {

    public boolean isKnowledgeQusestion(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }

        return containsAny(
                question,
                "궁금", "질문", "검색", "찾아줘", "찾아 줘", "알려줘", "설명", "정리",
                "무엇", "뭐야", "왜", "어떻게", "언제", "차이", "비교", "사용법",
                "패턴", "싱글톤", "빌더", "팩토리",
                "객체", "생성자", "클래스", "구현체",
                "자바", "java", "lombok",
                "디자인", "설계"
        );
    }

    private boolean containsAny(String text, String... keywords) {
        String normalizedText = text.toLowerCase();
        for (String keyword : keywords) {
            if (normalizedText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
