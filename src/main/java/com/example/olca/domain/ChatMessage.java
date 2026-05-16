package com.example.olca.domain;

import com.example.olca.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "chat_messag")
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_Id")
    private Session sessionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;


}
