package com.example.olca.ai.routing;

public record ExecutionPolicy(
        IntentRoute route,
        boolean ragAllowed,
        boolean ragRequired,
        boolean cacheableAnswer,
        String preResponse,
        String notFoundAnswer,
        String cacheSkipReason
) {
    /**
     * 일반 대화는 지식 검색과 캐시 저장을 모두 막아 일상 대화가 학습 데이터처럼 남지 않게 합니다.
     */
    public static ExecutionPolicy conversation() {
        return new ExecutionPolicy(
                IntentRoute.CONVERSATION,
                false,
                false,
                false,
                null,
                null,
                "conversation_route"
        );
    }

    /**
     * 학습형 질문은 검색 성공을 필수 조건으로 두어 자료 없는 답변 생성을 차단합니다.
     */
    public static ExecutionPolicy knowledge() {
        return new ExecutionPolicy(
                IntentRoute.KNOWLEDGE,
                true,
                true,
                true,
                "관련 자료를 확인하고 있습니다.",
                "저장된 관련 자료를 찾지 못했습니다. 키워드를 조금 더 구체적으로 말씀해 주시면 다시 확인하겠습니다.",
                "cacheable_knowledge_answer"
        );
    }

    /**
     * 시나리오 정리는 사용자의 입력 조건을 구조화하는 작업이므로 검색 실패에도 답변을 허용합니다.
     */
    public static ExecutionPolicy scenario() {
        return new ExecutionPolicy(
                IntentRoute.SCENARIO,
                true,
                false,
                false,
                null,
                null,
                "scenario_route"
        );
    }

    /**
     * 코드 작성은 명시 요청일 때만 허용하고 결과를 캐시에 저장하지 않습니다.
     */
    public static ExecutionPolicy codeRequest() {
        return new ExecutionPolicy(
                IntentRoute.CODE_REQUEST,
                true,
                false,
                false,
                null,
                null,
                "code_request_route"
        );
    }
}
