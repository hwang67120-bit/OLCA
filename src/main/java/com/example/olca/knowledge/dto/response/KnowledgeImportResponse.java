package com.example.olca.knowledge.dto.response;

import java.util.List;

public record KnowledgeImportResponse(
        String sourceTitle,
        int chunkCount,
        int savedCount,
        List<String> topics
) {

}
