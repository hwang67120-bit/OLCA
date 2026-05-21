package com.example.olca.domain;

import com.example.olca.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "chat_messag")
@Getter
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_Id")
    private Session session;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Builder
    public ChatMessage(Session session, String question, String answer) {
        this.session = session;
        this.question = question;
        this.answer = answer;
    }
}
