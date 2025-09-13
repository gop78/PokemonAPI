// PokemonRepository.java (Set을 사용한 대안)
package com.pokeapi.repository;

import com.pokeapi.entity.Pokemon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Long> {

    Optional<Pokemon> findByName(String name);
    Optional<Pokemon> findByEnglishName(String englishName);

    /**
     * 포켓몬과 타입 정보 함께 조회 (@EntityGraph 사용)
     */
    @EntityGraph(attributePaths = {"types", "types.type"})
    @Query("SELECT p FROM Pokemon p WHERE p.id = :id")
    Optional<Pokemon> findByIdWithTypesGraph(@Param("id") Long id);

    /**
     * 이름으로 포켓몬과 타입 정보 조회 (@EntityGraph 사용)
     */
    @EntityGraph(attributePaths = {"types", "types.type", "abilities", "abilities.ability"})
    @Query("SELECT p FROM Pokemon p WHERE p.name = :name OR p.englishName = :name")
    Optional<Pokemon> findByNameWithAllGraph(@Param("name") String name);

    /**
     * ID로 포켓몬과 모든 정보 조회 (@EntityGraph 사용)
     */
    @EntityGraph(attributePaths = {"types", "types.type", "abilities", "abilities.ability"})
    @Query("SELECT p FROM Pokemon p WHERE p.id = :id")
    Optional<Pokemon> findByIdWithAllGraph(@Param("id") Long id);

    /**
     * 모든 포켓몬과 연관 정보 조회 (페이지네이션)
     */
    @EntityGraph(attributePaths = {"types", "types.type", "abilities", "abilities.ability"})
    @Query("SELECT p FROM Pokemon p")
    Page<Pokemon> findAllWithAllGraph(Pageable pageable);

    /**
     * 포켓몬과 모든 연관 정보 함께 조회 (두 번의 쿼리로 - MultipleBagFetchException 방지)
     */
    @Query("SELECT DISTINCT p FROM Pokemon p " +
            "LEFT JOIN FETCH p.types pt " +
            "LEFT JOIN FETCH pt.type " +
            "WHERE p.id = :id")
    Optional<Pokemon> findByIdWithAllRelations(@Param("id") Long id);

    /**
     * 포켓몬과 타입 정보만 함께 조회
     */
    @Query("SELECT DISTINCT p FROM Pokemon p " +
            "LEFT JOIN FETCH p.types pt " +
            "LEFT JOIN FETCH pt.type " +
            "WHERE p.id = :id")
    Optional<Pokemon> findByIdWithTypes(@Param("id") Long id);

    /**
     * 포켓몬과 특성 정보만 함께 조회
     */
    @Query("SELECT DISTINCT p FROM Pokemon p " +
            "LEFT JOIN FETCH p.abilities pa " +
            "LEFT JOIN FETCH pa.ability " +
            "WHERE p.id = :id")
    Optional<Pokemon> findByIdWithAbilities(@Param("id") Long id);

    /**
     * 이름으로 포켓몬과 모든 연관 정보 조회 (두 번의 쿼리로 - MultipleBagFetchException 방지)
     */
    @Query("SELECT DISTINCT p FROM Pokemon p " +
            "LEFT JOIN FETCH p.types pt " +
            "LEFT JOIN FETCH pt.type " +
            "WHERE p.name = :name OR p.englishName = :name")
    Optional<Pokemon> findByNameWithAllRelations(@Param("name") String name);

    /**
     * 이름으로 포켓몬과 타입 정보 조회
     */
    @Query("SELECT DISTINCT p FROM Pokemon p " +
            "LEFT JOIN FETCH p.types pt " +
            "LEFT JOIN FETCH pt.type " +
            "WHERE p.name = :name OR p.englishName = :name")
    Optional<Pokemon> findByNameWithTypes(@Param("name") String name);

    /**
     * 이름으로 포켓몬과 특성 정보 조회
     */
    @Query("SELECT DISTINCT p FROM Pokemon p " +
            "LEFT JOIN FETCH p.abilities pa " +
            "LEFT JOIN FETCH pa.ability " +
            "WHERE p.name = :name OR p.englishName = :name")
    Optional<Pokemon> findByNameWithAbilities(@Param("name") String name);

    /**
     * 특정 타입을 가진 포켓몬들 조회
     */
    @Query("SELECT DISTINCT p FROM Pokemon p " +
            "JOIN p.types pt " +
            "JOIN pt.type t " +
            "WHERE t.name = :typeName OR t.koreanName = :typeName")
    Page<Pokemon> findByTypeName(@Param("typeName") String typeName, Pageable pageable);

    /**
     * 특정 특성을 가진 포켓몬들 조회
     */
    @Query("SELECT DISTINCT p FROM Pokemon p " +
            "JOIN p.abilities pa " +
            "JOIN pa.ability a " +
            "WHERE a.name = :abilityName OR a.koreanName = :abilityName")
    Page<Pokemon> findByAbilityName(@Param("abilityName") String abilityName, Pageable pageable);

    /**
     * 숨겨진 특성을 가진 포켓몬들 조회
     */
    @Query("SELECT DISTINCT p FROM Pokemon p " +
            "JOIN p.abilities pa " +
            "WHERE pa.isHidden = true")
    List<Pokemon> findPokemonWithHiddenAbilities();

    /**
     * 다중 타입 포켓몬들 조회
     */
    @Query("SELECT p FROM Pokemon p WHERE SIZE(p.types) > 1")
    List<Pokemon> findPokemonWithMultipleTypes();

    /**
     * 최대 ID 조회
     */
    @Query("SELECT COALESCE(MAX(p.id), 0) FROM Pokemon p")
    Optional<Long> findMaxId();

    boolean existsByName(String name);
    boolean existsByEnglishName(String englishName);
}