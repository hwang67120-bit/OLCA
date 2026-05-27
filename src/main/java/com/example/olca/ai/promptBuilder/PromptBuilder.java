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

    private static final String SYSTEM_PROMPT = """
            당신의 이름은 Elijah 입니다.  OLCA(Online Learning Chat Assistant) 프로젝트라는 프로그램 도움미 입니다.
                        사용자의 질문에 정확하고 자세하게 한국어로 답변하세요.
            
                        규칙:
                                    1. 한국어로만 답변하세요
                                    2. 컨텍스트가 제공되면 활용하여 답변하세요
                                    3. 모르는 내용은 모른다고 솔직하게 말하세요
                                    4. 기술적 질문에는 예제 코드를 포함하세요
                                    5. 자세한 사항을 요청이 없을시 5줄 이상 말하지 마세요
            """;
    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildUserPrompt(PromptContext context) {
        String knowledge = formatKnowledge(context.relatedKnowledge());
        String tags = formatTags(context.relatedTags());
        String history = formatHistory(context.pastMessages());
        String question = formatQuestion(context.question());

        return knowledge + tags + history + question;
    }

    private String formatKnowledge(List<KnowledgeBase> knowledge) {
        if (knowledge == null || knowledge.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("=== 관련 지식 ===\n");
        for (KnowledgeBase kb : knowledge) {
            sb.append("[").append(kb.getTopic()).append("]\n")
                    .append(kb.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    private String formatTags(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) return "";

        String tagNames = tags.stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));

        return "=== 관련 태그 ===\n" + tagNames + "\n\n";
    }

    private String formatHistory(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("=== 이전 대화 ===\n");
        for (ChatMessage msg : messages) {
            sb.append("Q: ").append(msg.getQuestion()).append("\n")
                    .append("A: ").append(msg.getAnswer()).append("\n\n");
        }
        return sb.toString();
    }

    private String formatQuestion(String question) {
        return "=== 질문 ===\n" + question;
    }
}