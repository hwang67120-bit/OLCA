package com.example.olca.session.repository;

import com.example.olca.session.domain.Session;
import com.example.olca.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session,Long> {

    List<Session> findByUser(User user);

}
