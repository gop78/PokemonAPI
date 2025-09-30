package com.pokeapi.entity;

import com.pokeapi.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Transactional
public class CascadeAndOrphanRemovalTest {

    @Autowired
    private TypeRepository typeRepository;

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PokemonAbilityRepository pokemonAbilityRepository;

    @Autowired
    private PokemonTypeRepository pokemonTypeRepository;

    @Test
    @DisplayName("1. CascadeType.PERSIST 테스트")
    void testCascadePersist() {
        // given
        Pokemon pikachu = new Pokemon();
        pikachu.setId(2L);
        pikachu.setName("피카츄");
        pikachu.setEnglishName("Pikachu");

        Type electricType = typeRepository.findByName("electric").orElseThrow();
        PokemonType pokemonType = new PokemonType(pikachu, electricType, 1);

        // 연관관계 설정
        pikachu.getTypes().add(pokemonType);

        // when
        // 부모만 저장
        pokemonRepository.save(pikachu); // PokemonType도 함께 저장됨

        entityManager.flush();
        entityManager.clear();

        // then
        Pokemon savedPokemon = pokemonRepository.findById(pikachu.getId()).orElseThrow();
        assertThat(savedPokemon.getTypes()).hasSize(1);
    }

    @Test
    @DisplayName("2. CascadeType.REMOVE 테스트")
    void testCascadeRemove() {
        // given
        Pokemon pokemon = pokemonRepository.findById(1L).orElseThrow();
        Long pokemonTypeId = pokemon.getTypes().get(0).getId();

        // when
        pokemonRepository.delete(pokemon);      // PokemonType도 함께 삭제

        entityManager.flush();

        // then
        assertThat(pokemonRepository.findById(1L)).isEmpty();
        assertThat(pokemonTypeRepository.findById(pokemonTypeId)).isEmpty();
    }

    @Test
    @DisplayName("3. CascadeType.MERGE 테스트")
    void testCascadeMerge() {
        // given
        // 준영속 상태 엔티티 생성
        Pokemon detachedPokemon = new Pokemon();
        detachedPokemon.setId(1L);
        detachedPokemon.setName("피카츄");
        detachedPokemon.setEnglishName("Pikachu");

        Type electricType = typeRepository.findByName("grass").orElseThrow();

        PokemonType detachedType = new PokemonType();
        detachedType.setId(1L);
        detachedType.setSlot(2);    // 기존과 다른 값
        detachedType.setPokemon(detachedPokemon);
        detachedType.setType(electricType);

        detachedPokemon.getTypes().add(detachedType);

        // when
        // 부모 병합
        Pokemon mergedPokemon = pokemonRepository.save(detachedPokemon);    // PokemonType도 함께 병합

        // Pokemon 병합 시 PokemonType도 자동 병합됨
    }

    @Test
    @DisplayName("4. Cascade 없이 저장 시도")
    void testNoCascade() {
        // given
        Pokemon pokemon = new Pokemon();
        pokemon.setId(1L);
        pokemon.setName("피카츄");
        pokemon.setEnglishName("Pikachu");
        PokemonType pokemonType = new PokemonType();
        pokemonType.setPokemon(pokemon);

        pokemon.getTypes().add(pokemonType);

        // when & then
        assertThatThrownBy(() -> {
            pokemonRepository.save(pokemon); // PokemonType은 저장 안됨 -> 오류 발생
            entityManager.flush();
        }).isInstanceOf(Exception.class);

    }

    @Test
    @DisplayName("5. OrphanRemoval = true 동작")
    void testOrphanRemovalTrue() {
        // Given - 특성이 있는 포켓몬
        Pokemon pokemon = pokemonRepository.findByIdWithAbilities(1L).orElseThrow();
        assertThat(pokemon.getAbilities()).isNotEmpty();

        PokemonAbility firstAbility = pokemon.getAbilities().get(0);
        Long abilityId = firstAbility.getId();

        System.out.println("=== 제거 전 특성 목록 ===");
        pokemon.getAbilities().forEach(ability ->
                System.out.println("- " + ability.getAbility().getName())
        );

        // When - 컬렉션에서 제거 (부모 관계 끊기)
        pokemon.getAbilities().remove(firstAbility);  // 이것만으로 DB 삭제!

        pokemonRepository.save(pokemon);
        entityManager.flush();
        entityManager.clear();

        // Then - 확인
        assertThat(pokemonAbilityRepository.findById(abilityId)).isEmpty();

        Pokemon reloadedPokemon = pokemonRepository.findByIdWithAbilities(1L).orElseThrow();
        System.out.println("=== 제거 후 특성 목록 ===");
        reloadedPokemon.getAbilities().forEach(ability ->
                System.out.println("- " + ability.getAbility().getName())
        );
    }

    @Test
    @DisplayName("6. OrphanRemoval = false 동작")
    void testOrphanRemovalFalse() {
        // given OrphanRemoval이 false인 관계 (PokemonType)
        Pokemon pokemon = pokemonRepository.findById(1L).orElseThrow();
        assertThat(pokemon.getAbilities()).isNotEmpty();

        PokemonType firstType = pokemon.getTypes().get(0);
        Long typeId = firstType.getId();

        // when
        pokemon.getTypes().remove(firstType); // 관계 끊어짐
        firstType.setPokemon(null); // 명시적으로 관계 해제

        pokemonRepository.save(pokemon);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<PokemonType> orphan = pokemonTypeRepository.findById(typeId);
        assertThat(orphan).isPresent(); // DB에 존재
        assertThat(orphan.get().getPokemon()).isNull(); // 부모 관계는 끊어짐
    }
}
