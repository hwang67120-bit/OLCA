package com.example.olca.repository;

import com.example.olca.domain.KnowledgeBase;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KnowledgeBaseRepository extends MongoRepository<KnowledgeBase,Long> {
}
