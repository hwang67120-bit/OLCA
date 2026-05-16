package com.example.olca.domain;

import com.example.olca.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;


@Entity
@Table(name = "session")
@Getter
public class Session extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User userId;

    @Column(nullable = false)
    private String title;


}
