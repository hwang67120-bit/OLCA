package com.example.olca.domain;

import com.example.olca.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(
        name = "tag",
        indexes = {
                @Index(name = "idx_tag_name", columnList = "name")
        }
)
@Getter
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer count;

    @Builder
    public Tag(String name, Integer count) {
        this.name = name;
        this.count = count != null ? count : 1;
    }

    protected Tag() {}

    // 카운터 증가 메서드
    public void incrementCount() {
        this.count++;
    }
}
