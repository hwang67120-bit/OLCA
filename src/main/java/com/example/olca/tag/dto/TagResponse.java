package com.example.olca.tag.dto;

import com.example.olca.tag.domain.Tag;

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
