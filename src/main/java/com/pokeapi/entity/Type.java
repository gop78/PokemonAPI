package com.pokeapi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 포켓몬 타입 엔티티
 */
@Entity
@Table(name = "types")
public class Type {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "korean_name", length = 50)
    private String koreanName;

    /**
     * 양방향 관계: Type과 PokemonType
     * mappedBy = "Type": PokemonType 엔티티의 type 필드와 연결
     * cascade = CascadeType.ALL: Type 삭제시 관련 PokemonType도 삭제
     * orphanRemoval = true: 고아 객체 자동 삭제
     */
    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PokemonType> pokemonTypes = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Type() {}

    // 기본 생성자 (JPA 필수)
    public Type(String name, String koreanName) {
        this.name = name;
        this.koreanName = koreanName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public void setKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }

    public List<PokemonType> getPokemonTypes() {
        return pokemonTypes;
    }

    public void setPokemonTypes(List<PokemonType> pokemonTypes) {
        this.pokemonTypes = pokemonTypes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Type{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", koreanName='" + koreanName + '\'' +
                ", pokemonTypes=" + pokemonTypes +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
