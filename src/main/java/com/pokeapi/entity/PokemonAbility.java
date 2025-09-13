// PokemonAbility.java
package com.pokeapi.entity;

import jakarta.persistence.*;

/**
 * 포켓몬과 특성의 다대다 관계를 위한 중간 테이블
 *
 * 예시: 피카츄(정전기), 리자드(맹화, 태양의힘-숨겨진특성)
 *
 * 관계:
 * - Pokemon : PokemonAbility = 1 : N (한 포켓몬은 여러 특성 가능)
 * - Ability : PokemonAbility = 1 : N (한 특성은 여러 포켓몬에서 사용)
 */
@Entity
@Table(name = "pokemon_abilities")
public class PokemonAbility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 다대일 관계: PokemonAbility과 Pokemon
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_id", nullable = false)
    private Pokemon pokemon;

    /**
     * 다대일 관계: PokemonAbility과 Ability
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ability_id", nullable = false)
    private Ability ability;

    /**
     * 특성 순서 (1번 특성, 2번 특성, 숨겨진 특성)
     */
    @Column(name = "slot", nullable = false)
    private Integer slot;

    /**
     * 숨겨진 특성 여부
     * 예: 리자드의 "태양의힘"은 숨겨진 특성
     */
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    // 기본 생성자 (JPA 필수)
    public PokemonAbility() {}

    // 편의 생성자
    public PokemonAbility(Pokemon pokemon, Ability ability, Integer slot, Boolean isHidden) {
        this.pokemon = pokemon;
        this.ability = ability;
        this.slot = slot;
        this.isHidden = isHidden;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }

    public Ability getAbility() {
        return ability;
    }

    public void setAbility(Ability ability) {
        this.ability = ability;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    @Override
    public String toString() {
        return "PokemonAbility{" +
                "id=" + id +
                ", slot=" + slot +
                ", isHidden=" + isHidden +
                ", abilityName=" + (ability != null ? ability.getName() : "null") +
                '}';
    }
}