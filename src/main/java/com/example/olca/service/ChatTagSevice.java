package com.example.olca.service;

import com.example.olca.domain.ChatMessage;
import com.example.olca.domain.ChatTag;
import com.example.olca.domain.Tag;
import com.example.olca.repository.ChatTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatTagSevice {

    private final ChatTagRepository chatTagRepository;

    @Transactional
    public void ilnk(ChatMessage chatMessage, Tag tag) {
        ChatTag chatTag = ChatTag.builder()
                .chatMessage(chatMessage)
                .tag(tag)
                .build();
        chatTagRepository.save(chatTag);
    }

    @Transactional
    public void linkMultiple(ChatMessage chatMessag, List<Tag> tags) {
        for (Tag tag : tags) {
            ChatTag chatTag = ChatTag.builder()
                    .chatMessage(chatMessag)
                    .tag(tag)
                    .build();
            chatTagRepository.save(chatTag);
        }
    }

    public List<ChatMessage> findByTag(Tag tag) {
        return chatTagRepository.findByTag(tag)
                .stream()
                .map(ChatTag::getChatMessage)
                .distinct()
                .toList();
    }

    @Transactional
    public void deletByMessage(ChatMessage chatMessage) {
        chatTagRepository.deleteByChatMessage(chatMessage);
    }
}
