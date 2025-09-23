package com.pokeapi;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_pokemon")
public class TestPokemon {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 모든 생명주기 콜백
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        System.out.println("@PrePersist 호출: " + name + " 저장 준비");
    }

    @PostPersist
    protected void afterCreate() {
        System.out.println("@PostPersist 호출: " + name + " 저장 완료! ID=" + id);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        System.out.println("@PreUpdate 호출: " + name + " 수정 준비");
    }

    @PostUpdate
    protected void afterUpdate() {
        System.out.println("@PostUpdate 호출: " + name + " 수정 완료!");
    }

    @PreRemove
    protected void beforeDelete() {
        System.out.println("@PreRemove 호출: " + name + " 삭제 준비");
    }

    @PostRemove
    protected void afterDelete() {
        System.out.println("@PostRemove 호출: " + name + " 삭제 완료!");
    }

    // 기본 생성자
    public TestPokemon() {}

    public TestPokemon(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}