package com.pokeapi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 포켓몬 특성 엔티티
 *
 * 테이블: abilities
 * 관계: Ability 1 : N PokemonAbility
 */
@Entity
@Table(name = "abilities")
public class Ability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "korean_name", length = 100)
    private String koreanName;

    /**
     * 양방향 관계: Ability과 PokemonAbility
     */
    @OneToMany(mappedBy = "ability", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PokemonAbility> pokemonAbilities = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }

    // 기본 생성자
    public Ability() {}

    // 편의 생성자
    public Ability(String name, String koreanName) {
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

    public List<PokemonAbility> getPokemonAbilities() {
        return pokemonAbilities;
    }

    public void setPokemonAbilities(List<PokemonAbility> pokemonAbilities) {
        this.pokemonAbilities = pokemonAbilities;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    @Override
    public String toString() {
        return "Ability{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", koreanName='" + koreanName + '\'' +
                ", pokemonAbilities=" + pokemonAbilities +
                ", createdAt=" + createdAt +
                ", updateAt=" + updateAt +
                '}';
    }
}
