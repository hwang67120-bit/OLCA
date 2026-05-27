package com.example.olca.chat.domain;

import com.example.olca.global.entity.BaseEntity;
import com.example.olca.session.domain.Session;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "chat_message",
indexes = {
        @Index(name = "idx_session", columnList = "session_id"),
        @Index(name = "idx_created", columnList = "createdAt")
})
@Getter
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session sessionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Builder
    public ChatMessage(Session sessionId, String question, String answer) {
        this.sessionId = sessionId;
        this.question = question;
        this.answer = answer;
    }
    protected ChatMessage() {}
}
