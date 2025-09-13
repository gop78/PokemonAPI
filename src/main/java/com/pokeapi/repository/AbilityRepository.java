package com.pokeapi.repository;

import com.pokeapi.entity.Ability;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AbilityRepository extends JpaRepository<Ability, Long> {

    /**
     * 영어 이름으로 특성 조회
     * 예: findByName("static") -> 정전기 특성
     */
    Optional<Ability> findByName(String name);

    /**
     * 한국어 이름으로 특성 조회
     * 예: findByKoreanName("정전기") -> static 특성
     */
    Optional<Ability> findByKoreanName(String koreanName);

    /**
     * 특성 이름으로 검색 (영어 + 한국어)
     */
    @Query("SELECT a FROM Ability a WHERE a.name LIKE %:keyword% OR a.koreanName LIKE %:keyword%")
    List<Ability> findByNameContaining(@Param("keyword") String keyword);

    /**
     * 모든 특성을 이름 순으로 조회
     */
    @Query("SELECT a FROM Ability a ORDER BY a.name")
    List<Ability> findAllOrderByName();
}
