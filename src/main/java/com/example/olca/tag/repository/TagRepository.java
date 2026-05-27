package com.example.olca.tag.repository;

import com.example.olca.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag,Long> {

    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
    @Query("SELECT t FROM  Tag  t ORDER BY  t.count DESC  LIMIT : limit")
    List<Tag> findTopTags(@Param("limit") int limit);
}
