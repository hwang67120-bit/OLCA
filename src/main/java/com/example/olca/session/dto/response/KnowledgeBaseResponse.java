package com.example.olca.session.dto.response;

import com.example.olca.knowledge.domain.KnowledgeBase;
import java.time.LocalDateTime;
import java.util.List;

public record KnowledgeBaseResponse(
        String id,
        String topic,
        String content,
        List<String> keywords,
        Integer version,
        LocalDateTime createdAt
) {
    public static KnowledgeBaseResponse from(KnowledgeBase knowledgeBase) {
        return new KnowledgeBaseResponse(
                knowledgeBase.getId(),
                knowledgeBase.getTopic(),
                knowledgeBase.getContent(),
                knowledgeBase.getKeywords(),
                knowledgeBase.getVersion(),
                knowledgeBase.getCreatedAt()
        );
    }
}
