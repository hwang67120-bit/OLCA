package com.example.olca.repository;

import com.example.olca.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session,Long> {

    List<Session> findByUserId(Long userId);

}
