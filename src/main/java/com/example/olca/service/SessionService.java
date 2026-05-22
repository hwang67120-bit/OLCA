package com.example.olca.service;

import com.example.olca.domain.Session;
import com.example.olca.domain.User;
import com.example.olca.dto.request.SessionCreateRequest;
import com.example.olca.dto.response.SessionResponse;
import com.example.olca.repository.SessionRepository;
import com.example.olca.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;


    @Transactional
    public SessionResponse create(SessionCreateRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));


        Session session = Session.builder()
                .user(user)
                .title(request.title())
                .build();


        Session savedSession = sessionRepository.save(session);

        return SessionResponse.from(savedSession);
    }


    public List<SessionResponse> findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sessionRepository.findByUser(user).stream()
                .map(SessionResponse::from)
                .toList();
    }


    public SessionResponse findById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        return SessionResponse.from(session);
    }
}