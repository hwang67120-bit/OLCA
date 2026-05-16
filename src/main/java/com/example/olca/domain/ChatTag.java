package com.example.olca.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "chat_tag")
@Getter
public class ChatTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
