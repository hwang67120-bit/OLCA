package com.example.olca.repository;

import com.example.olca.domain.ChatMessage;
import com.example.olca.domain.ChatTag;
import com.example.olca.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatTagRepository extends JpaRepository<ChatTag,Long> {
    
    @Query("SELECT ct.chatMessage FROM ChatTag ct WHERE ct.tag = : tag")
    List<ChatMessage> findChatMessagesByTag(@Param("tag") Tag tag);
    
    @Query("SELECT ct.tag FROM  ChatTag ct WHERE ct.chatMessage = :chatMessage")
    List<Tag> findTagsByMessage(@Param("chatMessage")ChatMessage chatMessage);

    void deleteByChatMessage(ChatMessage chatMessage);
}
