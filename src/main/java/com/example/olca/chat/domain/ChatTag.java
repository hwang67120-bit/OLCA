package com.example.olca.chat.domain;

import com.example.olca.tag.domain.Tag;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(
        name = "chat_tag",
        indexes = {
                @Index(name = "idx_chat_message", columnList = "chat_message_id"),
                @Index(name = "idx_tag", columnList = "tag_id")
        }
)
@Getter
public class ChatTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Builder
    public ChatTag(ChatMessage chatMessage, Tag tag) {
        this.chatMessage = chatMessage;
        this.tag = tag;
    }

    protected ChatTag() {
    }
}
