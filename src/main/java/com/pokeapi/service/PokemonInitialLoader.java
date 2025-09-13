package com.pokeapi.service;

import com.pokeapi.client.PokemonClient;
import com.pokeapi.entity.Ability;
import com.pokeapi.entity.Pokemon;
import com.pokeapi.entity.Type;
import com.pokeapi.model.PokemonResponse;
import com.pokeapi.model.PokemonSpeciesResponse;
import com.pokeapi.repository.AbilityRepository;
import com.pokeapi.repository.PokemonRepository;
import com.pokeapi.repository.TypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * CommandLineRunner: 애플리케이션 시작 후 자동으로 실행되는 컴포넌트
 * 초기 데이터를 로드하는 용도로 사용
 */
@Component
public class PokemonInitialLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PokemonInitialLoader.class);

    private final PokemonRepository pokemonRepository;

    private final TypeRepository typeRepository;

    private final AbilityRepository abilityRepository;

    private final PokemonClient pokemonClient;

    /**
     * @Value: application.properties에서 값을 주입받음
     * 기본값 설정 가능 (콜론 뒤가 기본값)
     */
    @Value("${app.data.initial-load-count:151}")
    private int initialLoadCount;   // 처음 로드할 포켓몬 수

    @Value("${app.data.initial-load-enabled:true}")
    private boolean initialLoadEnabled; // 초기 로드 활성화 여부

    @Value("${app.data.batch-size:10}")
    private int batchSize;  // 배치 크기

    /**
     * 생성자 주입: Spring이 자동으로 의존성을 주입해줌
     */
    public PokemonInitialLoader(PokemonRepository pokemonRepository, TypeRepository typeRepository, AbilityRepository abilityRepository, PokemonClient pokemonClient) {
        this.pokemonRepository = pokemonRepository;
        this.typeRepository = typeRepository;
        this.abilityRepository = abilityRepository;
        this.pokemonClient = pokemonClient;
    }

    /**
     * 애플리케이션 시작 후 자동 실행되는 메서드
     */
    @Override
    public void run(String... args) {
        // 설정에서 비활성화된 경우 실행하지 않음
        if (!initialLoadEnabled) {
            log.info("초기 포켓몬 데이터 로드가 비활성화되어 있습니다.");
            return;
        }

        // 이미 데이터가 있는지 확인
        long existingCount = pokemonRepository.count(); // JpaRepository의 기본 메서드
        if (existingCount >= initialLoadCount) {
            log.info("데이터베이스에 이미 {}마리의 포켓몬이 있습니다. 초기 로드를 건너뜁니다.", existingCount);
            logDatabaseStats();
            return;
        }

        log.info("초기 포켓몬 데이터 로드를 시작합니다. (목표: {}마리)", initialLoadCount);
        loadInitialPokemonData();
        logDatabaseStats();
    }

    /**
     * 실제 데이터 로드 로직
     */
    private void loadInitialPokemonData() {
        int loadedCount = 0;
        int failedCount = 0;

        // 배치 단위로 처리
        for (int start = 1; start <= initialLoadCount; start += batchSize) {
            int end = Math.min(start + batchSize - 1, initialLoadCount);
            log.info("포켓몬 배치 처리 중: {} - {} 번", start, end);

            // 배치 내의 각 포켓몬 처리
            for (int id = start; id <= end; id++) {
                try {
                    // 이미 존재하는지 확인 (중복 방지)
                    if (pokemonRepository.existsById((long) id)) {
                        log.debug("포켓몬 ID {}는 이미 존재합니다. 건너뜁니다.", id);
                        loadedCount++;
                        continue;
                    }

                    // PokeAPI에서 데이터 가져와서 저장
                    Pokemon savedPokemon = loadAndSavePokemon(id);
                    if (savedPokemon != null) {
                        loadedCount++;
                        log.debug("포켓몬 저장 완료: {} (ID: {})", savedPokemon.getName(), id);
                    } else {
                        failedCount++;
                    }

                } catch (Exception e) {
                    failedCount++;
                    log.warn("포켓몬 ID {} 로드 실패: {}", id, e.getMessage());
                }
            }

            // API 부하 방지를 위한 대기
            try {
                Thread.sleep(1000);  // 1초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("초기 로드가 중단되었습니다.", e);
                break;
            }
        }

        long finalCount = pokemonRepository.count();
        log.info("초기 포켓몬 데이터 로드 완료!");
        log.info("성공: {}마리, 실패: {}마리, DB 총 개수: {}마리", loadedCount, failedCount, finalCount);
    }

    /**
     * 개별 포켓몬을 로드하고 저장하는 메서드
     */
    private Pokemon loadAndSavePokemon(int id) {
        try {
            // 1. PokeAPI에서 기본 정보 가져오기
            PokemonResponse apiResponse = pokemonClient.getPokemon(String.valueOf(id));
            if (apiResponse == null) {
                log.warn("포켓몬 ID {}의 API 응답이 null입니다.", id);
                return null;
            }

            // 2. PokeAPI에서 Species 정보 가져오기 (한국어 이름용)
            PokemonSpeciesResponse speciesResponse = null;
            try {
                speciesResponse = pokemonClient.getPokeSpecies(String.valueOf(id));
            } catch (Exception e) {
                log.debug("포켓몬 ID {}의 Species 정보를 가져올 수 없습니다: {}", id, e.getMessage());
            }

            // 3. API 응답을 Entity로 변환
            Pokemon pokemon = convertToEntity(apiResponse, speciesResponse);

            // 4. 데이터베이스에 저장
            Pokemon savePokemon = pokemonRepository.save(pokemon);  // JpaRepository의 save 메서드
            return savePokemon;
        } catch (Exception e) {
            log.error("포켓몬 ID {} 로드 중 오류 발생", id, e);
            return null;
        }
    }

    /**
     * API 응답을 Entity로 변환하는 메서드
     */
    private Pokemon convertToEntity(PokemonResponse apiResponse, PokemonSpeciesResponse speciesResponse) {
        Pokemon pokemon = new Pokemon();

        // 기본 정보 설정
        pokemon.setId((long) apiResponse.getId());
        pokemon.setEnglishName(apiResponse.getName());
        pokemon.setHeight(apiResponse.getHeight());
        pokemon.setWeight(apiResponse.getWeight());

        // 한국어 이름 설정
        String koreanName = apiResponse.getName();  // 기본값은 영어 이름
        if (speciesResponse != null && speciesResponse.getNames() != null) {
            koreanName = speciesResponse.getNames().stream()
                    .filter(name -> "ko".equals(name.getLanguage().getName()))
                    .findFirst()
                    .map(PokemonSpeciesResponse.Name::getName)
                    .orElse(apiResponse.getName());
        }
        pokemon.setName(koreanName);

        // 스프라이트 URL 설정
        if (apiResponse.getSprites() != null && apiResponse.getSprites().getFront_default() != null) {
            pokemon.setSpriteUrl(apiResponse.getSprites().getFront_default());
        }

        // 타입 관계 설정
        if (apiResponse.getTypes() != null) {
            for (com.pokeapi.model.Type typeSlot : apiResponse.getTypes()) {
                Type type = getOrCreateType(typeSlot.getType().getName());
                pokemon.addType(type, typeSlot.getSlot());
                log.debug("타입 추가: {} (슬롯: {})", type.getName(), typeSlot.getSlot());
            }
        }

        // 특성 관계 설정
        if (apiResponse.getAbilities() != null) {
            for (com.pokeapi.model.Ability abilitySlot : apiResponse.getAbilities()) {
                Ability ability = getOrCreateAbility(abilitySlot.getAbility().getName());
                pokemon.addAbility(ability, abilitySlot.getSlot(), abilitySlot.isHidden());
                log.debug("특성 추가: {} (슬롯: {}, 숨김:{}",
                        ability.getName(), abilitySlot.getSlot(), abilitySlot.isHidden());
            }
        }

        return pokemon;
    }

    private Type getOrCreateType(String name) {
        Optional<Type> existingType = typeRepository.findByName(name);
        if (existingType.isPresent()) {
            return existingType.get();
        }

        // 새 타입 생성
        Type newType = new Type();
        newType.setName(name);
        newType.setKoreanName(translateTypeToKorean(name));
        Type savedType = typeRepository.save(newType);

        log.info("새 타입 생성: {} ({})", name, savedType.getKoreanName());
        return savedType;
    }

    private Ability getOrCreateAbility(String name) {
        Optional<Ability> existingAbility = abilityRepository.findByName(name);
        if (existingAbility.isPresent()) {
            return existingAbility.get();
        }

        // 새 특성 생ㄷ성
        Ability newAbility = new Ability();
        newAbility.setName(name);
        newAbility.setKoreanName(translateAbilityToKorean(name));
        Ability savedAbility = abilityRepository.save(newAbility);

        log.info("새 특성 생성: {} ({})", name, savedAbility.getKoreanName());
        return savedAbility;
    }

    /**
     * 타입 영어 -> 한국어 번역
     */
    private String translateTypeToKorean(String englishName) {
        switch (englishName.toLowerCase()) {
            case "fire": return "불꽃";
            case "water": return "물";
            case "grass": return "풀";
            case "electric": return "전기";
            case "psychic": return "에스퍼";
            case "ice": return "얼음";
            case "dragon": return "드래곤";
            case "dark": return "악";
            case "fairy": return "페어리";
            case "normal": return "노말";
            case "fighting": return "격투";
            case "poison": return "독";
            case "ground": return "땅";
            case "flying": return "비행";
            case "bug": return "벌레";
            case "rock": return "바위";
            case "ghost": return "고스트";
            case "steel": return "강철";
            default: return englishName;
        }
    }

    /**
     * 특성 영어 -> 한국어 번역
     */
    private String translateAbilityToKorean(String englishName) {
        switch (englishName.toLowerCase()) {
            case "static": return "정전기";
            case "lightning-rod": return "피뢰침";
            case "overgrow": return "신록";
            case "chlorophyll": return "엽록소";
            case "blaze": return "맹화";
            case "solar-power": return "태양의힘";
            case "torrent": return "급류";
            case "rain-dish": return "젖은접시";
            case "keen-eye": return "날카로운눈";
            case "tangled-feet": return "얽힌발";
            case "big-pecks": return "부풀린가슴";
            case "guts": return "근성";
            case "hustle": return "의욕";
            case "inner-focus": return "정신력";
            case "early-bird": return "일찍기상";
            case "scrappy": return "배짱";
            case "shed-skin": return "탈피";
            case "marvel-scale": return "이상한비늘";
            case "intimidate": return "위협";
            case "hyper-cutter": return "괴력집게";
            case "sand-veil": return "모래숨기";
            case "poison-point": return "독가시";
            case "rivalry": return "투쟁심";
            case "sheer-force": return "우격다짐";
            case "cute-charm": return "헤롱헤롱바디";
            case "magic-guard": return "매직가드";
            case "flash-fire": return "타오르는불꽃";
            case "run-away": return "도주";
            case "synchronize": return "싱크로";
            case "clear-body": return "클리어바디";
            case "natural-cure": return "자연회복";
            case "serene-grace": return "하늘의은총";
            case "swift-swim": return "쓱쓱";
            case "thick-fat": return "두꺼운지방";
            case "vital-spirit": return "의기양양";
            case "white-smoke": return "하얀연기";
            case "pure-power": return "순수한힘";
            case "shell-armor": return "조가비갑옷";
            case "air-lock": return "에어록";
            case "motor-drive": return "전기엔진";
            case "mold-breaker": return "틀깨기";
            case "super-luck": return "대운";
            case "aftermath": return "유폭";
            case "anticipation": return "위험예지";
            case "forewarn": return "예고";
            case "unaware": return "천진";
            case "tinted-lens": return "색안경";
            case "filter": return "필터";
            case "slow-start": return "슬로스타트";
            default: return englishName;
        }
    }

    /**
     * 데이터베이스 통계 출력
     */
    private void logDatabaseStats() {
        try {
            long pokemonCount = pokemonRepository.count();
            long typeCount = typeRepository.count();
            long abilityCount = abilityRepository.count();

            log.info("=== 데이터베이스 현황 ===");
            log.info("포켓몬: {}마리", pokemonCount);
            log.info("타입: {}개", typeCount);
            log.info("특성: {}개", abilityCount);

            if (pokemonCount > 0 && typeCount > 0) {
                // 타입별 포켓몬 수 (상위 5개)
                log.info("주요 타입별 포켓몬 수:");
                typeRepository.findAllOrderByName().stream()
                        .limit(5)
                        .forEach(type -> {
                            long count = pokemonRepository.findByTypeName(type.getName(),
                                    PageRequest.of(0, 1000)).getTotalElements();
                            log.info("  - {} ({}): {}마리", type.getName(), type.getKoreanName(), count);
                        });
            }

            log.info("========================");

        } catch (Exception e) {
            log.warn("데이터베이스 통계 출력 중 오류: {}", e.getMessage());
        }
    }
}
