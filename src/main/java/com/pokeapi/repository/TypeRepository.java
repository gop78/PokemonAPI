package com.pokeapi.repository;

import com.pokeapi.entity.Type;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeRepository extends JpaRepository<Type, Long> {

    /**
     * 영어 이름으로 타입 조회
     * 예: findByName("fire") -> 불꽃 타입
     */
    Optional<Type> findByName(String name);

    /**
     * 한국어 이름으로 타입 조회
     * 예: findByKoreanName("불꽃") -> fire 타입
     */
    Optional<Type> findByKoreanName(String koreanName);

    /**
     * 타입 이름으로 검색 (영어 + 한국어)
     */
    @Query("SELECT t FROM Type t WHERE t.name LIKE %:keyword% OR t.koreanName LIKE %:keyword%")
    List<Type> findByNameContaining(@Param("keyword") String keyword);

    /**
     * 모든 타입을 이름 순으로 조회
     */
    @Query("SELECT t FROM Type t ORDER BY t.name")
    List<Type> findAllOrderByName();
}
