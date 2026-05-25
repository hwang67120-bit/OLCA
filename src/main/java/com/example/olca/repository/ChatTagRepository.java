package com.example.olca.repository;

import com.example.olca.domain.ChatMessage;
import com.example.olca.domain.ChatTag;
import com.example.olca.domain.Tag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatTagRepository extends JpaRepository<ChatTag, Long> {

    @EntityGraph(attributePaths = {"chatMessage"})
    @Query("SELECT ct FROM ChatTag ct WHERE ct.tag = :tag")
    List<ChatTag> findByTag(@Param("tag") Tag tag);

    @EntityGraph(attributePaths = {"tag"})
    @Query("SELECT ct FROM ChatTag ct WHERE ct.chatMessage = :chatMessage")
    List<ChatTag> findByMessage(@Param("chatMessage") ChatMessage chatMessage);


    @EntityGraph(attributePaths = {"tag"})
    @Query("SELECT ct FROM ChatTag ct WHERE ct.tag.name IN :tagNames")
    List<ChatTag> findByTagNames(@Param("tagNames") List<String> tagNames);

    void deleteByChatMessage(ChatMessage chatMessage);
}
