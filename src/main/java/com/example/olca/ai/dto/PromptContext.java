package com.example.olca.ai.dto;

import com.example.olca.chat.domain.ChatMessage;
import com.example.olca.knowledge.domain.KnowledgeBase;
import com.example.olca.tag.domain.Tag;

import java.util.List;

public record PromptContext(
        String question,
        List<ChatMessage> pastMessages,
        List<Tag> relatedTags,
        List<KnowledgeBase> relatedKnowledge
) {
}
