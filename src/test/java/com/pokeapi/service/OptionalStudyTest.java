package com.pokeapi.service;

import com.pokeapi.entity.Pokemon;
import com.pokeapi.entity.Type;
import com.pokeapi.repository.PokemonRepository;
import com.pokeapi.repository.TypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Optional 활용 학습 테스트")
class OptionalStudyTest{

    @Mock
    private PokemonRepository pokemonRepository;

    @Mock
    private TypeRepository typeRepository;

    @Test
    @DisplayName("테스트1. Optional.of() vs Optional.ofNullable() - 안전한 Optional 생성")
    void testOptionalCreation() {
        // Optional.of() - null이면 NPE 발생
        Pokemon pokemon = createTestPokemon();
        Optional<Pokemon> safeOptional = Optional.of(pokemon);

        assertThat(safeOptional).isPresent();
        assertThat(safeOptional.get()).isEqualTo(pokemon);

        // Optional.ofNullable() - null일 수 있을 때 안전하게 사용
        Pokemon nullPokemon = null;
        Optional<Pokemon> nullableOptional = Optional.ofNullable(nullPokemon);

        assertThat(nullableOptional.isEmpty());

        // Optional.of(null)은 NPE 발생
        assertThatThrownBy(() -> Optional.of(nullableOptional))
                .isInstanceOf(NullPointerException.class);
    }

    // 테스트1 헬퍼 메서드
    private Pokemon createTestPokemon() {
        Pokemon pokemon = new Pokemon();
        pokemon.setId(1L);
        pokemon.setName("피카츄");
        pokemon.setEnglishName("pikachu");
        pokemon.setWeight(60);
        return pokemon;
    }

    @Test
    @DisplayName("테스트2. orElse() vs orElseGet() - 기본값 처리 방식의 차이")
    void testOrElseVsOrElseGet() {
        // orElse() - 항상 평가됨 (값이 있어도 기본값을 생성)
        Optional<Type> emptyType = Optional.empty();
        Type result1 = emptyType.orElse(createExpensiveType("default1"));

        assertThat(result1.getName()).isEqualTo("default1");

        // orElseGet() - 값이 없을 때만 평가됨 (더 효율적)
        Type result2 = emptyType.orElseGet(() -> createExpensiveType("default2"));

        assertThat(result2.getName()).isEqualTo("default2");

        // 값이 있는 경우 비교 - 여기서 차이가 드러남
        Optional<Type> presentType = Optional.of(createTestType());

        // orElse는 값이 있어도 기본값을 생성 (비효율적)
        Type result3 = presentType.orElse(createExpensiveType("unnecessary"));

        // orElseGetd은 값이 있으면 createExpensiveType를 호출하지 않음 (효율적)
        // 콘솔에 'won't be called'는 나오지 않음
        Type result4 = presentType.orElseGet(() -> createExpensiveType("won't be called"));

        assertThat(result3.getName()).isEqualTo("fire");
        assertThat(result4.getName()).isEqualTo("fire");

    }

    // 테스트2 헬퍼 메서드
    private Type createExpensiveType(String name) {
        System.out.println("실행: " + name);
        Type type = new Type();
        type.setName(name);
        return type;
    }

    private Type createTestType() {
        Type type = new Type();
        type.setName("fire");
        return type;
    }

    @Test
    @DisplayName("테스트3. map() - Optional 값 변환")
    void testOptionalMap() {
        // given
        Pokemon pokemon = createTestPokemon();
        when(pokemonRepository.findById(1L)).thenReturn(Optional.of(pokemon));
        when(pokemonRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then - 값이 있는 경우 반환
        Optional<String> pokemonName = pokemonRepository.findById(1L)
                .map(Pokemon::getName);

        assertThat(pokemonName).isPresent();
        assertThat(pokemonName.get()).isEqualTo("피카츄");

        // when & then - 값이 없는 경우 빈 Optional 반환
        Optional<String> emptyName = pokemonRepository.findById(999L)
                .map(Pokemon::getName);

        assertThat(emptyName).isEmpty();

        // 체이닝을 통한 안전환 변환
        Optional<Integer> nameLength = pokemonRepository.findById(1L)
                .map(Pokemon::getName)  // String
                .map(String::length);   // Integer

        assertThat(nameLength).isPresent();
        assertThat(nameLength.get()).isEqualTo(3);

        Optional<String> upperCaseName = pokemonRepository.findById(1L)
                .map(Pokemon::getName)
                .map(String::toUpperCase)
                .map(name -> "포켓몬: " + name);

        assertThat(upperCaseName.get()).isEqualTo("포켓몬: 피카츄");
    }

    @Test
    @DisplayName("테스트4. filter() - 조건부 Optional 처리")
    void testOptionalFilter() {
        // given
        Pokemon lightPokemon = createTestPokemon(); //  무게: 60
        Pokemon heavyPokemon = createHeavyPokemon();//  무게: 2100

        when(pokemonRepository.findById(1L)).thenReturn(Optional.of(lightPokemon));
        when(pokemonRepository.findById(2L)).thenReturn(Optional.of(heavyPokemon));

        // when & then - 조건에 맞는 경우
        Optional<Pokemon> filteredLight = pokemonRepository.findById(1L)
                .filter(p -> p.getWeight() < 100);

        assertThat(filteredLight).isPresent();
        assertThat(filteredLight.get().getName()).isEqualTo("피카츄");

        // when & then - 조건에 맞지 않는 경우
        Optional<Pokemon> filteredHeavy = pokemonRepository.findById(2L)
                .filter(p -> p.getWeight() < 100);

        assertThat(filteredHeavy).isEmpty();    // 조건 불만족시 빈 Optional 반환

        // 복합 필터링 체이닝
        Optional<String> lightPokemonName = pokemonRepository.findById(1L)
                .filter(p -> p.getWeight() < 100)        // 가벼운 포켓몬
                .filter(p -> p.getName().contains("츄")) // 이름에 "츄" 포함
                .map(Pokemon::getName);

        assertThat(lightPokemonName).isPresent();
        assertThat(lightPokemonName.get()).isEqualTo("피카츄");

        // 조건에 맞지 않으면 체이닝이 중단됨
        Optional<String> heavyPokemonName = pokemonRepository.findById(2L)
                .filter(p -> p.getWeight() < 100)   // 실패
                .map(Pokemon::getName);                      // 실행되지 않음

        assertThat(heavyPokemonName).isEmpty();
    }

    // 테스특4 헬퍼 메서드 추가
    private Pokemon createHeavyPokemon() {
        Pokemon pokemon = new Pokemon();
        pokemon.setId(2L);
        pokemon.setName("롱스톤");
        pokemon.setEnglishName("onix");
        pokemon.setWeight(2100);
        return pokemon;
    }

    @Test
    @DisplayName("ifPresent() - 조건부 실행")
    void testOptionalIfPresent() {
        // given
        Pokemon pokemon = createTestPokemon();
        when(pokemonRepository.findById(1L)).thenReturn(Optional.of(pokemon));
        when(pokemonRepository.findById(999L)).thenReturn(Optional.empty());

        // ifPresent - 값이 있을 때만 실행
        StringBuilder result1 = new StringBuilder();
        pokemonRepository.findById(1L)
                .ifPresent(p -> result1.append("Found: ").append(p.getName()));

        assertThat(result1.toString()).isEqualTo("Found: 피카츄");

        // 빈 Optional에서는 실행되지 않음
        StringBuilder result2 = new StringBuilder();
        pokemonRepository.findById(999L)
                .ifPresent(po -> result2.append("Found: ").append(po.getName()));

        assertThat(result2.toString()).isEmpty();

        // 실무에서 자주 쓰이는 패턴

        // 1. 로깅
        pokemonRepository.findById(1L)
                .ifPresent(p -> System.out.println("포켓몬 발견: " + p.getName()));

        // 2. 상태 변경
        StringBuilder status = new StringBuilder();
        pokemonRepository.findById(1L)
                .filter(p -> p.getWeight() < 100)
                .ifPresent(p -> status.append("가벼운 포켓몬: ").append(p.getName()));

        assertThat(status.toString()).isEqualTo("가벼운 포켓몬: 피카츄");

        // 위험한 패턴
        //if (optional.isPresent()) {
        //    Pokemon p = optional.get()
        //}

        // 올바른 방법
        pokemonRepository.findById(1L)
                .ifPresent(p -> {
                    // 이 영역에서 안전하게 처리
                    assertThat(p.getName()).isEqualTo("피카츄");
                });
    }

    @Test
    @DisplayName("orElseThrow() - 에외 처리")
    void testOptionalOrElseThrow() {
        // given
        when(pokemonRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then - 기본 예외 (NoSuchElementException)
        assertThatThrownBy(() ->
                pokemonRepository.findById(1L).orElseThrow())
                .isInstanceOf(NoSuchElementException.class);

        // when & then - 커스텀 예외와 처리
        assertThatThrownBy(() ->
                pokemonRepository.findById(1L)
                        .orElseThrow(() -> new RuntimeException("포켓몬을 찾을 수 없습니다: 1")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("포켓몬을 찾을 수 없습니다: 1");

        // 비즈니스 예외 던지기
        assertThatThrownBy(() ->
                pokemonRepository.findById(1L)
                        .orElseThrow(() -> new IllegalArgumentException("잘못된 포켓몬 ID")))
                .isInstanceOf(IllegalArgumentException.class);

        // 성공 케이스도 테스트
        Pokemon pokemon = createTestPokemon();
        when(pokemonRepository.findById(2L)).thenReturn(Optional.of(pokemon));

        Pokemon found = pokemonRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("이 예외는 발생하지 않음"));

        assertThat(found.getName()).isEqualTo("피카츄");
    }

    @Test
    @DisplayName("Repository 패턴에서의 Optional 활용")
    void testRepositoryPatternWithOptional() {
        // given
        Type fireType = createExpensiveType("fire");
        when(typeRepository.findByName("fire")).thenReturn(Optional.of(fireType));
        when(typeRepository.findByName("nonexistent")).thenReturn(Optional.empty());
        when(typeRepository.save(any(Type.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 패턴 1: 조회 후 예외 던지기
        Type existingType = typeRepository.findByName("fire")
                .orElseThrow(() -> new RuntimeException("타입을 찾을 수 없습니다."));

        assertThat(existingType.getName()).isEqualTo("fire");

        // 패턴 2: 조회 후 생성 (findOrCreate 패턴)
        Type newType = typeRepository.findByName("nonexistent")
                .orElseGet(() -> {
                    Type created =  new Type();
                    created.setName("nonexistent");
                    created.setKoreanName("새타입");
                    return typeRepository.save(created);
                });
        assertThat(newType.getName()).isEqualTo("nonexistent");
        assertThat(newType.getKoreanName()).isEqualTo("새타입");
    }

    // 헬퍼 메서드
    private Type createTestType(String name) {
        Type type = new Type();
        type.setName(name);
        return type;
    }

    @Test
    @DisplayName("Optional 안티패턴 - 피해야 할 사용법")
    void testOptionalAntiPatterns() {
        // given
        Pokemon pokemon = createTestPokemon();
        Optional<Pokemon> optionalPokemon = Optional.of(pokemon);
        Optional<Pokemon> emptyPokemon = Optional.empty();

        // 안티패턴 1: get() 직접 사용 (NoSuchElementException 위험)
        // Pokemon bad = emptyOptional.get();

        // 올바른 사용법: orElse, orElseGet, orElseThrow 사용
        Pokemon safePokemon1 = optionalPokemon.orElse(new Pokemon());
        Pokemon safePokemon2 = optionalPokemon.orElseGet(() -> new Pokemon());
        Pokemon safePokemon3 = optionalPokemon.orElseThrow(() -> new RuntimeException("없음"));

        assertThat(safePokemon1).isEqualTo(pokemon);
        assertThat(safePokemon2).isEqualTo(pokemon);
        assertThat(safePokemon3).isEqualTo(pokemon);

        // 안티패턴 2: isPresent() + get() whgkq
        // if (optionalPokemon.isPresent()) {
        //      Pokemon p = optionalPokemon.get();
        //      System.out.println(p.getName());
        // {

        // 올바른 사용법: ifPresent 사용
        StringBuilder result = new StringBuilder();
        optionalPokemon.ifPresent(p -> result.append(p.getName()));
        assertThat(result.toString()).isEqualTo("피카츄");

        // 안티패턴 3: Optional.of(null) 사용
        // Optional<Pokemon> badOptional = Optional.of(null)    // NPE 발생

        // 올바른 사용법: Optional.ofNullable() 사용
        Optional<Pokemon> goodOptional = Optional.ofNullable(null);
        assertThat(goodOptional).isEmpty();

        // 안티패턴 4: Optional을 필드로 사용
        // class BadClass {
        //      private Optional<String> name;
        // }

        // 올바른 방법: 메서드 리턴 타입으로만 사용
        // public Optional<Pokemon> findPokemon() { ... }
    }

    @Test
    @DisplayName("Optional 성능 고려사항 - OptionalInt, OptionalLong 사용")
    void testOptionalPrimitives() {
        // 비효율적: Boxing/Unboxing 발생
        Optional<Integer> boxedId = Optional.of(25);
        Optional<Double> boxedWeight = Optional.of(6.0);
        Optional<Long> boxedExp = Optional.empty();

        // 효율적: 기본형 Optional 사용
        java.util.OptionalInt pokemonId = java.util.OptionalInt.of(25);
        java.util.OptionalDouble pokemonWeight = java.util.OptionalDouble.of(6.0);
        java.util.OptionalLong pokemonExp = java.util.OptionalLong.empty();

        // 값 확인
        assertThat(pokemonId.isPresent()).isTrue();
        assertThat(pokemonId.getAsInt()).isEqualTo(25);

        assertThat(pokemonWeight.isPresent()).isTrue();
        assertThat(pokemonWeight.getAsDouble()).isEqualTo(6.0);

        assertThat(pokemonExp.isEmpty()).isTrue();

        // orElse 사용
        int id = pokemonId.orElse(0);
        double weight = pokemonWeight.orElse(0.0);
        long exp = pokemonExp.orElse(1000L);

        assertThat(id).isEqualTo(25);
        assertThat(weight).isEqualTo(6.0);
        assertThat(exp).isEqualTo(1000L);

        // 성능이 중요한 곳에서 사용
        calculateSomething().ifPresent(value ->
                System.out.println("결과: " + value));
    }

    private java.util.OptionalInt calculateSomething() {
        return java.util.OptionalInt.of(42);
    }
}