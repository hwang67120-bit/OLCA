package com.example.olca.knowledge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record KnowledgeBaseSaveRequest(
        @NotBlank
        @Size(max = 100)
        String topic,

        @NotBlank
        @Size(min = 10, max = 10000)
        String content,

        List<String> keywords
        ) {
}
