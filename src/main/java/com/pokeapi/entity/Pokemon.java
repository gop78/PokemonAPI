package com.pokeapi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity: 데이터베이스 테이블과 매핑되는 클래스
 * 이 클래스는 'pokemon' 테이블과 연결됩니다
 *
 * 관계:
 * - Pokemon 1 : N PokemonType (포켓몬은 여러 타입 가능)
 * - Pokemon 1 : N PokemonAbility (포켓몬은 여러 특성 가능)
 */
@Entity // JPA에게 이 클래스가 데이터베이스 테이블임을 알려줍니다
@Table(name = "pokemon")    // 실제 데이블 이름을 지정 (생략 시 클래스명과 동일)
public class Pokemon {

    /**
     * @Id: 기본키(Primary Key)를 나타냅니다
     * PokeAPI의 ID를 그대로 사용하므로 @GeneratedValue는 사용하지 않습니다
     */
    @Id
    private Long id;

    /**
     * @Column: 테이블의 컬럼과 매핑
     * nullable = false: NOT NULL 제약조건
     */
    @Column(nullable = false, length = 100)
    private String name;    // 한국어 이름

    @Column(name = "english_name", nullable = false, length = 100)  // 컬럼명이 필드명과 다를 때 지정
    private String englishName; // 영어 이름

    private Integer height; // @Column생략 시 필드명과 같은 컬럼명 사용

    private Integer weight;

    @Column(name = "sprite_url", length = 500)
    private String spriteUrl;   //  포켓몬 이미지 URL

    /**
     * 양방향 관계: Pokemon과 PokemonType
     *
     * mappedBy = "pokemon": PokemonType 엔티티의 pokemon 필드와 연결
     * cascade = CascadeType.ALL: Pokemon 저장/삭제시 PokemonType도 함께 처리
     * orphanRemoval = true: Pokemon에서 제거된 PokemonType 자동 삭제
     * fetch = FatchType.LAZY: 필요할 때만 로드 (성능 최적화)
     */
    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) // 테스트 코드
//    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY) // 테스트 코드
//    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.MERGE, fetch = FetchType.LAZY) // 테스트 코드
//    @OneToMany(mappedBy = "pokemon", fetch = FetchType.LAZY) // 테스트 코드
    private List<PokemonType> types = new ArrayList<>();

    /**
     * 양방향 관계: Pokemon과 PokemonAbility
     */
    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PokemonAbility> abilities = new ArrayList<>();

    /**
     * JAP에서 생성/수정 시간을 자동으로 관리하기 위한 필드들
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * @PrePersist: 엔티티가 처음 저장되기 전에 실행
     * @PreUpdate: 엔티티가 업데이트되기 전에 실행
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 기본 생성자 (JPA에서 필수)
    public Pokemon() {}

    // 퍈의 생성자
    public Pokemon(Long id, String name, String englishName) {
        this.id = id;
        this.name = name;
        this.englishName = englishName;
    }

    // === 연관관계 편의 메서드 ===

    /**
     * 타입 추가 (단방향으로 설정하여 LazyInitializationException 방지)
     */
    public void addType(Type type, Integer slot) {
        PokemonType pokemonType = new PokemonType(this,  type, slot);
        this.types.add(pokemonType);
    }

    /**
     * 특성 추가 (단방향으로 설정하여 LazyInitializationException 방지)
     */
    public void addAbility(Ability ability, Integer slot, Boolean isHidden) {
        PokemonAbility pokemonAbility = new PokemonAbility(this, ability, slot, isHidden);
        this.abilities.add(pokemonAbility);
    }

    /**
     * 타입 제거
     */
    public void removeType(PokemonType pokemonType) {
        this.types.remove(pokemonType);
        pokemonType.setPokemon(null);
        pokemonType.setType(null);
    }

    /**
     * 특성 제거
     */
    public void removeAbility(PokemonAbility pokemonAbility) {
        this.abilities.remove(pokemonAbility);
        pokemonAbility.setPokemon(null);
        pokemonAbility.setAbility(null);
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

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getSpriteUrl() {
        return spriteUrl;
    }

    public void setSpriteUrl(String spriteUrl) {
        this.spriteUrl = spriteUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<PokemonType> getTypes() {
        return types;
    }

    public void setTypes(List<PokemonType> types) {
        this.types = types;
    }

    public List<PokemonAbility> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<PokemonAbility> abilities) {
        this.abilities = abilities;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Pokemon{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", englishName='" + englishName + '\'' +
                ", typesCount='" + (types != null ? types.size() : 0) + '\'' +
                ", abilitiesCount=" + (abilities != null ? abilities.size() : 0) + '\'' +
                '}';
    }
}
