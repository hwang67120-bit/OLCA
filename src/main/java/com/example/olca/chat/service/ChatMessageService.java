package com.example.olca.chat.service;

import com.example.olca.chat.domain.ChatMessage;
import com.example.olca.session.domain.Session;
import com.example.olca.chat.dto.requst.ChatMessgeCreateRequset;
import com.example.olca.chat.dto.response.ChatMessageResponse;
import com.example.olca.chat.repository.ChatMessageRepository;
import com.example.olca.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final SessionRepository sessionRepository;

    @Transactional
    public ChatMessageResponse create(ChatMessgeCreateRequset requset) {

        Session session = sessionRepository.findById(requset.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("섹션을 찿을수 없습니다"));

        ChatMessage message = ChatMessage.builder()
                .sessionId(session)
                .question(requset.question())
                .answer(requset.answer())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        return ChatMessageResponse.from(saved);
    }

    public ChatMessageResponse findById(Long id) {
        ChatMessage message = chatMessageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다"));
        return ChatMessageResponse.from(message);
    }

    public List<ChatMessageResponse> findBySessionId(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다"));

        List<ChatMessage> messages = chatMessageRepository.findBySessionId(session);

        List<ChatMessageResponse> responses = new ArrayList<>();
        for (ChatMessage message : messages) {
            responses.add(ChatMessageResponse.from(message));
        }
        return responses;
    }
}

