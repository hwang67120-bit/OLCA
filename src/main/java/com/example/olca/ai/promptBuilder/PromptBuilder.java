package com.example.olca.ai.promptBuilder;

import com.example.olca.ai.dto.PromptContext;
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
            너의 이름은 Elijah다.
            너는 OLCA(Online Learning Chat Assistant)의 실시간 대화형 AI 도우미다.

            응답 원칙:
            1. 반드시 한국어로 답한다.
            2. 첫 답변은 2~4문장으로 제한한다.
            3. 학습형 질문도 먼저 핵심만 답하고, 자세한 설명은 사용자가 요청할 때 이어서 한다.
            4. 관련 지식이 제공되면 그 내용을 우선 사용한다.
            5. 근거가 부족하면 모른다고 말하고, 더 정확한 키워드를 요청한다.
            6. 코드 예시는 사용자가 요청할 때만 제공한다.
            7. 마지막에는 필요한 경우 짧은 후속 질문 1개만 던진다.
            8. 답변은 TTS로 읽히므로 사람에게 말하듯 자연스럽게 작성한다.
            9. Markdown 문법을 사용하지 않는다. 별표, 하이픈 목록, 번호 목록, 코드블록, 표를 쓰지 않는다.
            10. @, #, *, ` 같은 기호를 읽어야 하는 형태로 쓰지 않는다.
            11. 라이브러리 어노테이션은 "Builder 어노테이션"처럼 말로 풀어서 표현한다.
            """;

    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildUserPrompt(PromptContext context) {
        String knowledge = formatKnowledge(context.relatedKnowledge());
        String tags = formatTags(context.relatedTags());
        String history = formatHistory(context.pastMessages());
        String question = formatQuestion(context.question());

        return """
                [이번 답변 제한]
                500자 이내로 답한다.
                문서처럼 길게 설명하지 않는다.
                Markdown 문법, 별표 강조, 하이픈 목록, 번호 목록을 절대 쓰지 않는다.
                TTS가 읽기 좋게 짧은 대화체 문장으로 말한다.
                핵심 답변 후 필요하면 이어서 설명할지 물어본다.

                """
                + knowledge
                + tags
                + history
                + question;
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
