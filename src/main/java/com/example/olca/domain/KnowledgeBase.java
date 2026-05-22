package com.example.olca.domain;


import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collation = "knowledgeBase")
@Getter
public class KnowledgeBase {

    @Id
    private String id;

    private String topic;
    private String content;
    private List<String> keywords;
    private Integer version;

    @CreatedDate
    private LocalDateTime createdAt;
}