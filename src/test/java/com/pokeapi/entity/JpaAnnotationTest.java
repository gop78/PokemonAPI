package com.pokeapi.entity;

import com.pokeapi.TestPokemon;
import com.pokeapi.TestPokemonRepository;
import com.pokeapi.repository.PokemonRepository;
import com.pokeapi.repository.TypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional  // 각 테스트 후 자동 롤백
public class JpaAnnotationTest  {

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private TypeRepository typeRepository;

    @Autowired
    private TestPokemonRepository testPokemonRepository;

    /**
     * 실습1: @Entity와 @Table
     * - 엔티티 생성 및 저장 확인
     */
    @Test
    @DisplayName("1. @Entity와 @Table - 포켓몬 엔티티 생성 및 저장")
    void test_EntityAndTable() {
        // given: 포켓몬 객체 생성
        Pokemon testPokemon = new Pokemon();
        testPokemon.setId(999L);
        testPokemon.setName("테스트몬");
        testPokemon.setEnglishName("Testmon");
        testPokemon.setHeight(100);
        testPokemon.setWeight(100);

        // when: 저장 (JPA가 INSERT 쿼리 생성)
        Pokemon saved = pokemonRepository.save(testPokemon);

        pokemonRepository.flush();

        // then: 저장 확인
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(999L);
        assertThat(saved.getName()).isEqualTo("테스트몬");

        System.out.println("저장된 포켓몬: " + saved);
    }

    @Test
    @DisplayName("실습2: @Id와 @GeneratedValue - ID 생성 전략 비교")
    void test02_IdGeneration() {
        System.out.println("\n=== Case 1: Pokemon (수동 ID) ===");

        // Pokemon - @GeneratedValue 없음
        Pokemon pokemon = new Pokemon();
        pokemon.setId(998L);
        pokemon.setName("수동ID몬");
        pokemon.setEnglishName("ManualIdMon");

        Pokemon savedPokemon = pokemonRepository.save(pokemon);
        pokemonRepository.flush();

        System.out.println("설정한 ID: 998");
        System.out.println("저장된 ID: " + savedPokemon.getId());

        System.out.println("\n=== Case 2: Type (자동 ID) ===");

        // Type - @GeneratedValue 있음
        Type type = new Type();
        type.setName("testtype");
        type.setKoreanName("테스트타입");
        // ID 설정 없음

        Type savedType = typeRepository.save(type);
        typeRepository.flush();

        System.out.println("ID 설정 안 함");
        System.out.println("자동 생성된 ID: " + savedType.getId());

        // 검증
        assertThat(savedPokemon.getId()).isEqualTo(998L);
        assertThat(savedType.getId()).isNotNull();
        assertThat(savedType.getId()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("실습2-1: Pokemon에서 ID 없이 저장하면?")
    void test02_1_PokemonWithoutId() {
        Pokemon pokemon = new Pokemon();
        pokemon.setName("ID없는몬");
        pokemon.setEnglishName("NoIdMon");

        try {
            pokemonRepository.save(pokemon);
            pokemonRepository.flush();
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getClass().getSimpleName());
            System.out.println("메시지: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("실습2-2: 자동 ID 증가 확인")
    void test02_2_AutoIncrementTest() {
        // 첫 번째 타입
        Type type1 = new Type();
        type1.setName("fire2");
        type1.setKoreanName("불꽃");
        Type saved1 = typeRepository.save(type1);
        typeRepository.flush();

        System.out.println("1번 타입 ID: " + saved1.getId());

        // 두 번째 타입
        Type type2 = new Type();
        type2.setName("water2");
        type2.setKoreanName("물");
        Type saved2 = typeRepository.save(type2);
        typeRepository.flush();

        System.out.println("2반 타입 ID: " + saved2.getId());

        // 세 번째 타입
        Type type3 = new Type();
        type3.setName("grass2");
        type3.setKoreanName("풀");
        Type saved3 = typeRepository.save(type3);
        typeRepository.flush();

        System.out.println("3번 타입 ID: " + saved3.getId());

        // 검증
        assertThat(saved2.getId()).isGreaterThan(saved1.getId());
        assertThat(saved3.getId()).isGreaterThan(saved2.getId());
    }

    @Test
    @DisplayName("실습3: @Column - 컬럼 속성 테스트")
    void test03_ColumnAttriutes() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("@Column 속성 테스트");
        System.out.println("=".repeat(50) + "\n");

        Pokemon pokemon = new Pokemon();
        pokemon.setId(997L);

        // 1. nullable = false 테스트
        System.out.println("1. nullable = false 테스트");
        pokemon.setName("필수입력몬");           // NOT NULL
        pokemon.setEnglishName("RequireMon"); // NOT NULL
        System.out.println("   name (필수): " + pokemon.getName());
        System.out.println("   english_name (필수): " +  pokemon.getEnglishName());

        // 2. name 속성 테스트 (필드명 vs 컬럼명)
        System.out.println("\n2. name 속성 테스트 (필드명 != 컬럼명)");
        System.out.println("     자바 필드명: englishName");
        System.out.println("     DB 컬럼명: english_name");

        // 3. @Column 생략 테스트
        System.out.println("\n3. @Column 생략 테스트");
        pokemon.setHeight(150);
        pokemon.setWeight(300);
        System.out.println("     height 필드 -> height 컬럼");
        System.out.println("     weight 필드 -> weight 컬럼");

        /*
         * # INSERT 쿼리
         * insert into pokemon (created_at, english_name, height, name, sprite_url, updated_at, weight, id) values(?, ?, ?, ?, ?, ?, ?, ?)
         */
        Pokemon saved =  pokemonRepository.save(pokemon);
        pokemonRepository.flush();

        System.out.println("ID: " + saved.getId());
        System.out.println("     Name: " + saved.getName());
        System.out.println("     English Name: " + saved.getEnglishName());
        System.out.println("     Height: " + saved.getHeight());
        System.out.println("     Weight: " + saved.getWeight());

        // 검증
        assertThat(saved.getName()).isEqualTo("필수입력몬");
        assertThat(saved.getEnglishName()).isEqualTo("RequireMon");
        assertThat(saved.getHeight()).isEqualTo(150);

        System.out.println("\n" + "=".repeat(50) + "\n");

    }

    @Test
    @DisplayName("실습3-1: nullable = false 위반 시 에러")
    void test03_1_Nullableviolation() {
        Pokemon pokemon = new Pokemon();
        pokemon.setId(996L);
        pokemon.setName("정상몬");
        pokemon.setEnglishName("NormalMon");

        try {
            pokemonRepository.save(pokemon);
            pokemonRepository.flush();

            System.out.println("저장 성공 (name이 NULL이 아님)");

            // NULL로 변경 시도
            pokemon.setName(null);
            pokemonRepository.save(pokemon);
            pokemonRepository.flush();

        } catch (Exception e) {
            System.out.println("에러 발생");
            System.out.println("타입: " + e.getClass().getSimpleName());
            System.out.println("-> nullable = false 이므로 NULL 불가\n");
        }
    }

    @Test
    @DisplayName("실습3-2: unique = true 제약조건 테스트")
    void test03_2_UniqueConstraint() {
        // 첫 번째 타입 저장
        Type type1 = new Type();
        type1.setName("electric");
        type1.setKoreanName("전기");

        Type saved1 = typeRepository.save(type1);
        typeRepository.flush();

        System.out.println("첫 번째 타입 저장 성공");
        System.out.println("     name: " + saved1.getName());

        // 같은 name으로 두 번째 타입 저장 시도
        Type type2 = new Type();
        type2.setName("electric");
        type2.setKoreanName("전기2");

        try {
            typeRepository.save(type2);
            typeRepository.flush();
        } catch (Exception e) {
            System.out.println("에러 발생");
            System.out.println("타입: " + e.getClass().getSimpleName());
            System.out.println("-> unique = true 이므로 중복 불가\n");
        }
    }

    @Test
    @DisplayName("실습3-3: length 제약조건 테스트")
    void test03_3_LengthConstraint() {
        Pokemon pokemon = new Pokemon();
        pokemon.setId(995L);

        // length = 100 테스트
        String normalName = "정상길이이름";   // 7자
        String longName = "a".repeat(150); // 150자 (제한 100자 초과)

        System.out.println("1. 정상 길이 이름 (7자)");
        pokemon.setName(normalName);
        pokemon.setEnglishName("NormalLength");

        Pokemon saved =  pokemonRepository.save(pokemon);
        pokemonRepository.flush();

        System.out.println("저장 성공: " + saved.getName());

        System.out.println("\n2. 긴 이름 (150자 - 제한 100자 초과");
        System.out.println("     이름 길이: " + longName.length() + "자");

        pokemon.setName(longName);
        try {
            pokemonRepository.save(pokemon);
            pokemonRepository.flush();
        } catch (Exception e) {
            System.out.println("에러 발생");
            System.out.println("     타입: " + e.getClass().getSimpleName());
            System.out.println("     -> length 제약 위반\n");
        }
    }

    @Test
    @DisplayName("실습4: @OneToMany와 @ManyToOne - 관계 매핑")
    void test_04_OneToManyRelationship() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("@OneToMany와 @ManyToOne - 포켓몬과 타입 관계 설정");
        System.out.println("=".repeat(50) + "\n");

        // given
        Pokemon pikachu = new Pokemon();
        pikachu.setId(40L);
        pikachu.setName("피카츄2");
        pikachu.setEnglishName("Pikachu2");

        Type electricType = new Type();
        electricType.setName("electric2");
        electricType.setKoreanName("전기2");
        Type savedType = typeRepository.save(electricType);
        typeRepository.flush();
        System.out.println("   타입: " + savedType.getKoreanName() + " (ID: " + savedType.getId() + ")");

        // when
        pikachu.addType(savedType, 1);
        System.out.println("피카츄의 types 리스트에 전기타입 추가");

        Pokemon saved = pokemonRepository.save(pikachu);
        pokemonRepository.flush();

        // then
        assertThat(saved.getTypes()).hasSize(1);
        System.out.println("피카츄의 타입 개수: " + saved.getTypes().size());

        assertThat(saved.getTypes().get(0).getType().getName()).isEqualTo("electric");
        System.out.println("1번 타입: " + saved.getTypes().get(0).getType().getName());

        assertThat(saved.getTypes().get(0).getSlot()).isEqualTo(1);
        System.out.println("타입 순서(slot): " + saved.getTypes().get(0).getSlot());

        /*
            -- 1. Type 저장
            Hibernate:
                insert into types (name, korean_name) values (?, ?)

            -- 2. Pokemon 저장
            Hibernate:
                insert into pokemon (id, name, english_name, ...) values (?, ?, ?, ...)

            -- 3. PokemonType 저장 (cascade 덕분에 자동!)
            Hibernate:
                insert into pokemon_types (pokemon_id, type_id, slot)
                values (?, ?, ?)
         */
    }

    @Test
    @DisplayName("실습4-1: 복수 타입 포켓몬 (이상해씨 - 풀, 독)")
    void test_04_1_MultipleTypes() {
        // 포켓몬 생성
        Pokemon bulbasaur = new Pokemon();
        bulbasaur.setId(1L);
        bulbasaur.setName("이상해씨");
        bulbasaur.setEnglishName("Bulbasaur");

        // 타입 생성
        Type grassType = new Type();
        grassType.setName("grass");
        grassType.setKoreanName("풀");
        typeRepository.save(grassType);

        Type poisonType = new Type();
        poisonType.setName("poison");
        poisonType.setKoreanName("독");
        typeRepository.save(poisonType);

        typeRepository.flush();

        System.out.println("1. 타입 추가 (순서 중요)");
        bulbasaur.addType(grassType, 1);    // 1번 타입: 풀
        bulbasaur.addType(poisonType, 2);   // 2번 타입: 독

        // 저장
        Pokemon saved = pokemonRepository.save(bulbasaur);
        pokemonRepository.flush();

        assertThat(saved.getTypes()).hasSize(2);
        System.out.println("타입 개수: "  + saved.getTypes().size());

        System.out.println("   이상해씨의 타입들: ");
        saved.getTypes().forEach(pt -> {
            System.out.println("       - " + pt.getType().getKoreanName() + " (slot: " + pt.getSlot() + ")");
        });
    }

    @Test
    @DisplayName("실습4-2: cascade - 연쇄 저장")
    void test_04_2_Cascade() {
        Pokemon charmander = new Pokemon();
        charmander.setId(4L);
        charmander.setName("파이리");
        charmander.setEnglishName("Charmander");

        Type fireType = new Type();
        fireType.setName("fire");
        fireType.setKoreanName("불꽃");
        typeRepository.save(fireType);
        typeRepository.flush();

        charmander.addType(fireType, 1);

        System.out.println("1. Pokemon만 save() 호출");
        Pokemon saved = pokemonRepository.save(charmander);
        pokemonRepository.flush();

        System.out.println("2. cascade 덕분에 PokemonType도 자동 저장");

        assertThat(saved.getTypes()).hasSize(1);
        System.out.println(" 파이리의 타입: " +  saved.getTypes().get(0).getType().getName());
    }

    @Test
    @DisplayName("실습4-3: orphanRemoval - 고아 객체 제거")
    void test04_3_orphanRemoval() {
        // given: 타입이 있는 포켓몬 저장
        Pokemon squirtle = new Pokemon();
        squirtle.setId(7L);
        squirtle.setName("꼬부기");
        squirtle.setEnglishName("Squirtle");

        Type waterType = new Type();
        waterType.setName("water");
        waterType.setKoreanName("물");
        typeRepository.save(waterType);
        typeRepository.flush();

        squirtle.addType(waterType, 1);
        Pokemon saved = pokemonRepository.save(squirtle);
        pokemonRepository.flush();

        System.out.println("1. 저장 완료");
        assertThat(saved.getTypes()).hasSize(1);
        System.out.println("   타입 개수: " + saved.getTypes().size());

        // when: 타입 관계 제거
        System.out.println("\n2. types 리스트에서 제거");
        saved.getTypes().clear();
        pokemonRepository.save(saved);
        pokemonRepository.flush();

        // then: orphanRemoval = true 덕분에 PokemonType 삭제됨
        System.out.println("\n3. orphanRemoval로 PokemonType 자동 삭제");
        Pokemon update = pokemonRepository.findById(7L).orElse(null);
        assertThat(update.getTypes()).isEmpty();
        System.out.println("    타입 개수: " + update.getTypes().size());
    }

    @Test
    @DisplayName("실습5: 모든 생명주기 콜백 테스트")
    void test05_AllLifecycleCallbacks() {

        TestPokemon pokemon = new TestPokemon(999L, "테스트몬");

        // 저장
        System.out.println("1. 저장 시작");
        testPokemonRepository.save(pokemon);
        testPokemonRepository.flush();

        // 수정
        System.out.println("\n2. 수정 시작");
        pokemon.setName("수정된테스트몬");
        testPokemonRepository.save(pokemon);
        testPokemonRepository.flush();

        // 삭제
        System.out.println("\n3. 삭제 시작");
        testPokemonRepository.delete(pokemon);
        testPokemonRepository.flush();

    }
}