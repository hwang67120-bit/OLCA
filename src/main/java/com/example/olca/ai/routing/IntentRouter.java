package com.example.olca.ai.routing;

import com.example.olca.knowledge.service.KnowledgeDomainGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntentRouter {

    private final KnowledgeDomainGuard knowledgeDomainGuard;

    /**
     * 입력을 먼저 코드 작성, 시나리오, 학습, 대화 순서로 검증해서 가장 안전한 실행 정책을 선택합니다.
     */
    public IntentRoutingDecision route(String question) {
        if (question == null || question.isBlank()) {
            return new IntentRoutingDecision(
                    IntentRoute.CONVERSATION,
                    ExecutionPolicy.conversation(),
                    "empty_question",
                    "빈 질문"
            );
        }

        if (containsAny(question, "코드 작성", "코딩해줘", "구현해줘", "작성해줘", "만들어줘", "생성해줘", "컨트롤러 만들어", "서비스 만들어", "메서드 작성")) {
            return new IntentRoutingDecision(
                    IntentRoute.CODE_REQUEST,
                    ExecutionPolicy.codeRequest(),
                    "explicit_code_request",
                    "명시적 코드 작성 요청"
            );
        }

        if (containsAny(question, "시나리오", "로직", "흐름", "분기", "조건", "예외 처리", "입력 검증", "출력", "컴퓨팅적 사고", "구조 잡아")) {
            return new IntentRoutingDecision(
                    IntentRoute.SCENARIO,
                    ExecutionPolicy.scenario(),
                    "scenario_intent",
                    "시나리오 또는 로직 정리 요청"
            );
        }

        if (knowledgeDomainGuard.isKnowledgeQusestion(question)) {
            return new IntentRoutingDecision(
                    IntentRoute.KNOWLEDGE,
                    ExecutionPolicy.knowledge(),
                    "knowledge_intent",
                    "학습 의도 있음"
            );
        }

        return new IntentRoutingDecision(
                IntentRoute.CONVERSATION,
                ExecutionPolicy.conversation(),
                "no_knowledge_intent",
                "학습 의도 없음"
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
