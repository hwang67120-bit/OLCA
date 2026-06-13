package com.example.olca.ai.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chatFlow")
@Getter
public class ChatFlow {

    @Id
    private String id;

    private Long sessionId;
    private Long userId;
    private String question;

    private List<Long> relareMessageIds;
    private List<Long> relatedTagIds;
    private List<String> relatedKnowLedgeIds;

    private String answer;
    private boolean cacheable;

    @CreatedDate
    private LocalDateTime createAt;

    @Builder
    public ChatFlow(Long sessionId, Long userId, String question,
                    List<Long> relareMessageIds, List<Long> relatedTagIds,
                    List<String> relatedKnowLedgeIds, String answer, boolean cacheable) {

        this.sessionId = sessionId;
        this.userId = userId;
        this.question = question;
        this.relareMessageIds = relareMessageIds;
        this.relatedTagIds = relatedTagIds;
        this.relatedKnowLedgeIds = relatedKnowLedgeIds;
        this.answer = answer;
        this.cacheable = cacheable;
    }
}
