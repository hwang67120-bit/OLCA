package com.example.olca.ai.promptBuilder;

import com.example.olca.ai.dto.PromptContext;
import com.example.olca.ai.routing.IntentRoute;
import com.example.olca.ai.routing.IntentRoutingDecision;
import com.example.olca.chat.domain.ChatMessage;
import com.example.olca.knowledge.domain.KnowledgeBase;
import com.example.olca.tag.domain.Tag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    private static final int MAX_KNOWLEDGE_CONTENT_LENGTH = 350;
    private static final int MAX_HISTORY_ANSWER_LENGTH = 180;

    private static final String SYSTEM_PROMPT = """
            당신의 이름은 엘리아입니다.
            당신은 차분한 AI 비서이자 개발 보조 조수입니다.
            모든 사람을 존중하며 항상 정중한 존댓말로 답변합니다.

            주요 역할:
            1. 사용자가 까먹은 코딩 문법, 개념, 단어, 메서드명을 짧고 정확하게 알려줍니다.
            2. 한국어 표현을 개발에 맞는 영어 단어 또는 메서드명 후보로 바꿔 제안합니다.
            3. 사용자가 시나리오를 말하면 핵심 조건과 흐름을 정리해 로직으로 바꿀 수 있게 돕습니다.
            4. 아이디어를 정리하고, 필요한 경우 현실적인 대안을 2~3개만 제시합니다.

            응답 원칙:
            1. 먼저 결론을 짧게 말합니다.
            2. 답변은 간결하게 작성합니다.
            3. 한 번에 너무 많은 정보를 주지 않습니다.
            4. 필요한 경우 1~3단계로 나누어 설명합니다.
            5. 코드 작성은 사용자가 요청할 때만 합니다.
            6. 모르는 내용은 단정하지 않고 확인이 필요하다고 말합니다.
            7. 사용자가 놓친 예외나 위험은 조용히 짚어줍니다.

            금지 행동:
            1. 요청하지 않은 코드를 작성하지 않습니다.
            2. 불필요하게 긴 설명을 하지 않습니다.
            3. 사용자의 요청 범위를 벗어난 행동을 하지 않습니다.
            4. 확실하지 않은 내용을 사실처럼 말하지 않습니다.
            5. 별표, 샵, 골뱅이, 괄호, 밑줄 같은 특수문자를 불필요하게 읽지 않도록 답변합니다.
            6. 채팅창에만 필요한 마크다운 기호나 코드 기호를 TTS 답변에 포함하지 않습니다.

            출력 방식:
            1. 사람이 듣기 자연스러운 문장으로 답변합니다.
            2. 일반 설명은 짧은 문단으로 답변합니다.
            3. 문법 설명은 핵심 예시 1개만 제공합니다.
            4. 메서드명이나 영어 단어 제안은 2~3개 후보만 제공합니다.
            """;

    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildUserPrompt(PromptContext context) {
        return buildUserPrompt(context, null);
    }

    public String buildUserPrompt(PromptContext context, IntentRoutingDecision decision) {
        String knowledge = formatKnowledge(context.relatedKnowledge());
        String tags = formatTags(context.relatedTags());
        String history = formatHistory(context.pastMessages());
        String routeGuide = formatRouteGuide(decision);
        String answerLimit = formatAnswerLimit(decision);
        String question = formatQuestion(context.question());

        return answerLimit
                + routeGuide
                + knowledge
                + tags
                + history
                + question;
    }

    private String formatAnswerLimit(IntentRoutingDecision decision) {
        if (decision != null && decision.route() == IntentRoute.CODE_REQUEST) {
            return """
                    [이번 답변 제한]
                    700자 이내로 답변합니다.
                    요청된 코드만 짧게 작성합니다.
                    코드가 필요하면 핵심 예시 1개만 제공합니다.
                    특수문자나 코드 기호를 말로 풀어서 읽지 않습니다.
                    답을 확정하기 어렵다면 짧게 확인 질문을 합니다.

                    """;
        }

        return """
                [이번 답변 제한]
                500자 이내로 답변합니다.
                문서처럼 길게 설명하지 않습니다.
                Markdown 문법, 별표 강조, 표, 목록, 코드블록은 사용하지 않습니다.
                TTS가 읽기 좋은 짧은 대화체 문장으로 말합니다.
                답을 확정하기 어렵다면 짧게 확인 질문을 합니다.

                """;
    }

    private String formatRouteGuide(IntentRoutingDecision decision) {
        if (decision == null) {
            return "";
        }

        return switch (decision.route()) {
            case CONVERSATION -> """
                    [라우팅 정책]
                    일반 대화입니다. 자연스럽고 정중하게 짧게 답변합니다.

                    """;
            case KNOWLEDGE -> """
                    [라우팅 정책]
                    학습형 질문입니다. 제공된 관련 지식을 우선으로 답변합니다.
                    관련 지식이 부족하면 모르는 내용을 단정하지 않습니다.

                    """;
            case SCENARIO -> """
                    [라우팅 정책]
                    시나리오 또는 로직 정리 요청입니다.
                    입력, 처리, 출력, 예외 흐름을 중심으로 간단히 정리합니다.
                    관련 지식이 없어도 사용자가 준 조건 안에서 구조를 잡습니다.

                    """;
            case CODE_REQUEST -> """
                    [라우팅 정책]
                    명시적인 코드 작성 요청입니다.
                    사용자가 요청한 범위 안에서만 짧게 작성합니다.
                    필요한 경우 놓치기 쉬운 검증이나 예외를 한 문장으로 덧붙입니다.

                    """;
        };
    }

    private String formatKnowledge(List<KnowledgeBase> knowledge) {
        if (knowledge == null || knowledge.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("[관련 지식]\n");
        for (KnowledgeBase kb : knowledge) {
            sb.append("주제: ").append(kb.getTopic()).append("\n")
                    .append("내용: ").append(limit(kb.getContent(), MAX_KNOWLEDGE_CONTENT_LENGTH)).append("\n");
        }
        return sb.append("\n").toString();
    }

    private String formatTags(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) return "";

        String tagNames = tags.stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));

        return "[관련 태그]\n" + tagNames + "\n\n";
    }

    private String formatHistory(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("[이전 대화]\n");
        for (ChatMessage msg : messages) {
            sb.append("Q: ").append(msg.getQuestion()).append("\n")
                    .append("A: ").append(limit(msg.getAnswer(), MAX_HISTORY_ANSWER_LENGTH)).append("\n");
        }
        return sb.append("\n").toString();
    }

    private String formatQuestion(String question) {
        return "[사용자 질문]\n" + question;
    }

    private String limit(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }
}
