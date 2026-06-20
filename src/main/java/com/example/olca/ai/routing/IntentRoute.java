package com.example.olca.ai.routing;

public enum IntentRoute {
    CONVERSATION("대화형"),
    KNOWLEDGE("학습형"),
    SCENARIO("시나리오형"),
    CODE_REQUEST("코드작성형");

    private final String label;

    /**
     * 출력 로그에서 사람이 읽기 쉬운 의도 이름을 함께 보관합니다.
     */
    IntentRoute(String label) {
        this.label = label;
    }

    /**
     * 라우팅 결과를 로그와 디버깅 화면에서 확인할 수 있게 한글 라벨로 변환합니다.
     */
    public String label() {
        return label;
    }
}
