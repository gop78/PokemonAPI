package com.pokeapi.entity;

import jakarta.persistence.*;

/**
 * 포켓몬과 타입의 다대다 관계를 위한 중간 테이블
 *
 * 예시: 피카츄(전기), 파이리(불꽃), 이상해씨(풀, 독)
 *
 * 관계:
 * - Pokemon : PokemonType = 1 : N (한 포켓몬은 여러 타입 가능)
 * - Type : PokemonType = 1 : N (한 타입은 여러 포켓몬에서 사용)
 */
@Entity
@Table(name = "pokemon_types")
public class PokemonType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 다대일 관계: PokemonType과 Pokemon
     * fetch = FetchType.LAZY: 필요할 때만 로드 (성능 최적화)
     * JoinColumn: 외래키 컬럼명 지정
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_id", nullable = false)
    private Pokemon pokemon;

    /**
     * 다대일 관계: PokemonType과 Type
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private Type type;

    /**
     * 타입 순서 (1번타입, 2번타입)
     * 예: 이상해씨는 1번=풀, 2번=독
     */
    @Column(name = "slot", nullable = false)
    private Integer slot;

    // 기본 생성자
    public PokemonType() {}

    // 편의 생성자
    public PokemonType(Pokemon pokemon, Type type, Integer slot) {
        this.pokemon = pokemon;
        this.type = type;
        this.slot = slot;
    }

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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    @Override
    public String toString() {
        return "PokemonType{" +
                "id=" + id +
                ", slot=" + slot +
                ", type=" + (type != null ? type.getName() : "null") +
                '}';
    }
}
