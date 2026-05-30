package com.example.olca.knowledge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record KnowledgeImportRequest(

        @NotBlank
        @Size(max = 100)
        String sourceTitle,

        @NotBlank
        @Size(min = 30, max = 50000)
        String text,

        List<String> keywords
) {
}
