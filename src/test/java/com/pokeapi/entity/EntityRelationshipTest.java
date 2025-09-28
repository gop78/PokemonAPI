package com.pokeapi.entity;

import com.pokeapi.repository.PokemonRepository;
import com.pokeapi.repository.TypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class EntityRelationshipTest {
    @Autowired private
    PokemonRepository pokemonRepository;

    @Autowired private
    TypeRepository typeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("실습1: 양방향 관계의 주인")
    void test01_RelationshipOwner() {

        // 기본 데이터 생성
        Pokemon pikachu = new Pokemon();
        pikachu.setId(25L);
        pikachu.setName("피카츄");
        pikachu.setEnglishName("Pikachu");

        Type electricType = new Type();
        electricType.setName("electric");
        electricType.setKoreanName("전기");
        typeRepository.save(electricType);

        // Case 1: 잘못된 방법 - 주인이 아닌 쪽만 설정
        PokemonType pokemonType = new PokemonType();
        pokemonType.setSlot(1);
        pokemonType.setType(electricType);
        // pokemonType.setPokemon(pikachu); <- 관계의 주인 설정 안함

        // Pokemon의 리스트에만 추가
        pikachu.getTypes().add(pokemonType);

        pokemonRepository.save(pikachu);
        entityManager.flush();
        entityManager.clear();

        // 저장 데이터 확인
        Pokemon foundPokemon = pokemonRepository.findById(25L).orElse(null);
        System.out.println("피카츄의 타입 개수: " + foundPokemon.getTypes().size());

        if (foundPokemon.getTypes().isEmpty()) {
            System.out.println("관계의 주인을 설정하지 않았기 때문에 DB에 저장되지 않음");
        }

        // Case 2: 올바른 사용 방법
        pikachu.getTypes().clear(); // 기존 데이터 삭제
        pikachu.addType(electricType, 1);

        pokemonRepository.save(pikachu);
        entityManager.flush();
        entityManager.clear();

        // 저장 데이터 조회
        foundPokemon = pokemonRepository.findById(25L).orElse(null);
        System.out.println("피카츄의 타입 개수: " +  foundPokemon.getTypes().size());

        if (!foundPokemon.getTypes().isEmpty()) {
            System.out.println("-> DB에 저장 완료");
            System.out.println("-> 타입: " + foundPokemon.getTypes().get(0).getType());
        }

        assertThat(foundPokemon.getTypes()).hasSize(1);
    }

    @Test
    @DisplayName("실습2: FetchType.LAZY - 지연 로딩 동작 확인")
    void test02_LazyLoading() {
        // given
        setupTestData();

        // when
        // Pokemon만 조회
        Pokemon pokemon = pokemonRepository.findById(25L).orElse(null);
        System.out.println("Pokemon 조회 완료: " + pokemon.getName());
        System.out.println(" -> types 데이터는 로드되지 않음");
        System.out.println(" -> Proxy 객체만 생성됨\n");

        // then
        // types 접근 시점에 지연 로딩 발생
        List<PokemonType> types = pokemon.getTypes();
        System.out.println("types 접근 -> 지연 로딩 쿼리 실행");

        assertThat(types).isNotEmpty();
        System.out.println("피카츄의 타입 개수: " + types.size());

        // Type 정보 접근 시 또 다른 지연 로딩 발생
        String typeName = types.get(0).getType().getKoreanName();
        System.out.println("첫 번째 타입: " + typeName);

    }

    @Test
    @DisplayName("실습2-2: FetchType.EAGER")
    void test02_02_EagerLoading() {
        setupTestData();

        Pokemon pokemon = pokemonRepository.findByIdWithTypesGraph(25L).orElse(null);
        System.out.println("Pokemon + Types 조회 완료" + pokemon.getName());

        // 이미 모든 데이터가 로드된 상태
        List<PokemonType> types = pokemon.getTypes(); // DB 접근 없음
        System.out.println("타입 개수: " + types.size());

        String typeName = types.get(0).getType().getKoreanName(); // DB 접근 없음
        System.out.println("타입 이름: " + typeName);
    }

    private void setupTestData() {
        Type electricType = new Type();
        electricType.setName("electric");
        electricType.setKoreanName("전기");
        typeRepository.save(electricType);

        Pokemon pikachu = new Pokemon();
        pikachu.setId(25L);
        pikachu.setName("피카츄");
        pikachu.setEnglishName("Pikachu");
        pikachu.addType(electricType, 1);

        pokemonRepository.save(pikachu);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("실습3: N+1 문제 발생")
    void test03_NPlusOneProblem() {
        // given
        setupMultiplePokemons();

        // when
        // 모든 포켓몬 조회
        List<Pokemon> pokemons = pokemonRepository.findAll();

        // then
        // 각 포켓몬의 타입 접근 (N번 추가 쿼리)
        for (Pokemon pokemon : pokemons) {
            System.out.println("포켓몬: " + pokemon.getName());

            // 지연 로딩 쿼리 실행
            List<PokemonType> types = pokemon.getTypes();

            for (PokemonType pt : types) {
                // 또 다른 지원 로딩 쿼리
                System.out.println(pt.getType().getKoreanName() + " ");
            }
        }
        /*
         * 결과: 1 + N + M번의 쿼리 실행
         * - 1번: 포켓몬 목록 조회
         * - N번: 각 포켓몬의 타입 조회
         * - M번: 각 타입의 상세 정보 조회
         * - 예:포켓몬 3마리 -> 최대 7번 쿼리
         */
    }

    private void setupMultiplePokemons() {
        Type electricType = new Type();
        electricType.setName("electric");
        electricType.setKoreanName("전기");
        typeRepository.save(electricType);

        Type fireType = new Type();
        fireType.setName("fire");
        fireType.setKoreanName("불꽃");
        typeRepository.save(fireType);

        Type grassType = new Type();
        grassType.setName("grass");
        grassType.setKoreanName("풀");
        typeRepository.save(grassType);

        Type poisonType = new Type();
        poisonType.setName("poison");
        poisonType.setKoreanName("독");
        typeRepository.save(poisonType);

        Pokemon pikachu = new Pokemon();
        pikachu.setId(25L);
        pikachu.setName("피카츄");
        pikachu.setEnglishName("Pikachu");
        pikachu.addType(electricType, 1);
        pokemonRepository.save(pikachu);

        Pokemon charmander = new Pokemon();
        charmander.setId(4L);
        charmander.setName("파이리");
        charmander.setEnglishName("Charmander");
        charmander.addType(fireType, 1);
        pokemonRepository.save(charmander);

        Pokemon bulbasauir = new Pokemon();
        bulbasauir.setId(1L);
        bulbasauir.setName("이상해씨");
        bulbasauir.setEnglishName("Bulbasaur");
        bulbasauir.addType(grassType, 1);
        bulbasauir.addType(poisonType, 2);
        pokemonRepository.save(bulbasauir);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("실습4: @EntityGraph로 N+1 문제 해결")
    void test04_EntityGraphSolution() {

        // given
        setupMultiplePokemons();

        // when
        // 타입 정보만 조회
//        Page<Pokemon> pokemonPage = pokemonRepository.findAllWithAllGraph(
//                PageRequest.of(0, 10)
//        ); // -> MultipleBagFetchException 발생!!
//        List<Pokemon> pokemons = pokemonPage.getContent();

        Pokemon pokemonWithTypes = pokemonRepository.findByIdWithTypesGraph(25L).orElse(null);
        // @EntityGraph: {\"types\", \"types.type\"}"

        assertThat(pokemonWithTypes).isNotNull();
        assertThat(pokemonWithTypes.getTypes()).isNotEmpty();

        // 추가 쿼리 없이 타입 정보 사용 가능
        String typeName = pokemonWithTypes.getTypes().get(0).getType().getKoreanName();
        System.out.println("타입: " + typeName + " (추가 쿼리 없음!)");

        //entityManager.clear();

        // 특성 정보도 필ㄹ요한 경우 추가 조회
        Pokemon pokemonWithAbilities = pokemonRepository.findByIdWithAbilities(25L).orElse(null);
        // @Query : LEFT JOIN FETCH p.abilities pa LEFT JOIN FETCH pa.ability
        if (pokemonWithAbilities != null && !pokemonWithAbilities.getAbilities().isEmpty()) {
            String abilityName = pokemonWithAbilities.getAbilities().get(0).getAbility().getKoreanName();
            System.out.println("특성 : " + abilityName + " (추가 쿼리 없음!)");
        }
    }

    @Test
    @DisplayName("실습5: Fetch Join으로 N+1 문제 해결")
    void test05_FetchJoinSolution() {
        // given
        setupTestData();

        // when
        Optional<Pokemon> foundPokemon = pokemonRepository.findByIdWithAllRelations(25L);
        if (foundPokemon.isPresent()) {
            Pokemon pikachu = foundPokemon.get();
            System.out.println("1번의 JOIN 쿼리로 모든 데이터 조회");
            System.out.println("조회된 포켓몬: " + pikachu.getName() + "\n");

            // 추가 쿼리 없이 데이터 사용
            System.out.println("포켓몬: " + pikachu.getName());
            System.out.println("타입: ");
            for (PokemonType pt : pikachu.getTypes()) {
                System.out.println(pt.getType().getKoreanName() + " ");
            }
        }
    }
}
