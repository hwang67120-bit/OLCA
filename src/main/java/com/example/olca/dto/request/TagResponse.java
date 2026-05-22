package com.example.olca.dto.request;

import com.example.olca.domain.Tag;

import java.time.LocalDateTime;

public record TagResponse(
        Long id,
        String name,
        Integer count,
        LocalDateTime createAt
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getCount(),
                tag.getCreatedAt()
        );
    }
}
