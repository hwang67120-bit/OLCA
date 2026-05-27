package com.example.olca.knowledge.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "knowledgeBase")
@Getter
public class KnowledgeBase {

    @Id
    private String id;

    @TextIndexed(weight = 3)
    private String topic;

    @TextIndexed(weight = 2)
    private String content;

    private List<String> keywords;

    private Integer version;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public KnowledgeBase(String topic, String content, List<String> keywords, Integer version) {
        this.topic = topic;
        this.content = content;
        this.keywords = keywords;
        this.version = version != null ? version : 1;
    }
}