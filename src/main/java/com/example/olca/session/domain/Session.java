package com.example.olca.session.domain;

import com.example.olca.global.entity.BaseEntity;
import com.example.olca.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;


@Entity
@Table(name = "session")
@Getter
public class Session extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;


    @Builder
    public Session(User user, String title){
        this.user = user;
        this.title = title;
    }
    protected Session() {}
}
