// PokemonService.java (데이터 로딩 문제 해결)
package com.pokeapi.service;

import com.pokeapi.client.PokemonClient;
import com.pokeapi.entity.*;
import com.pokeapi.exception.ExternalApiException;
import com.pokeapi.exception.PokemonNotFoundException;
import com.pokeapi.exception.PokemonInvalidArgumentException;
import com.pokeapi.model.PokemonListResponse;
import com.pokeapi.model.PokemonResponse;
import com.pokeapi.model.PokemonSpeciesResponse;
import com.pokeapi.model.PokemonSummary;
import com.pokeapi.repository.AbilityRepository;
import com.pokeapi.repository.PokemonRepository;
import com.pokeapi.repository.TypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PokemonService {

    private static final Logger log = LoggerFactory.getLogger(PokemonService.class);

    private final PokemonRepository pokemonRepository;
    private final TypeRepository typeRepository;
    private final AbilityRepository abilityRepository;
    private final PokemonClient pokemonClient;

    public PokemonService(PokemonRepository pokemonRepository,
                          TypeRepository typeRepository,
                          AbilityRepository abilityRepository,
                          PokemonClient pokemonClient) {
        this.pokemonRepository = pokemonRepository;
        this.typeRepository = typeRepository;
        this.abilityRepository = abilityRepository;
        this.pokemonClient = pokemonClient;
    }

    /**
     * 개별 포켓몬 조회 (타입, 특성 정보 포함)
     */
    // @Cacheable(value = "pokemon", key = "#nameOrId") // 임시로 캐시 비활성화
    @Transactional(readOnly = true) // 명시적 트랜잭션 추가
    public PokemonResponse getPokemon(String nameOrId) {
        log.debug("포켓몬 조회 요청: {}", nameOrId);

        try {
            // 1. DB에서 먼저 조회 (관련 정보 포함)
            Pokemon pokemon = findPokemonInDatabaseWithRelations(nameOrId);

            if (pokemon != null) {
                log.debug("DB에서 포켓몬 발견: {} (ID: {})", pokemon.getName(), pokemon.getId());
                log.debug("타입: {}개, 특성: {}개 로드됨", 
                    pokemon.getTypes() != null ? pokemon.getTypes().size() : 0,
                    pokemon.getAbilities() != null ? pokemon.getAbilities().size() : 0);

                return convertToResponse(pokemon);
            }

            // 2. DB에 없으면 API에서 로드 후 저장
            log.info("DB에 없는 포켓몬, API에서 로드 시작: {}", nameOrId);
            pokemon = loadAndSavePokemonFromApi(nameOrId);

            log.info("새 포켓몬 로드 완료: {} (ID: {})", pokemon.getName(), pokemon.getId());
            return convertToResponse(pokemon);

        } catch (PokemonNotFoundException e) {
            log.warn("포켓몬 찾을 수 없음: {}", nameOrId);
            throw e;
        } catch (Exception e) {
            log.error("포켓몬 조회 중 예상치 못한 오류: {}", nameOrId, e);
            throw new PokemonNotFoundException("포켓몬 조회 중 오류가 발생했습니다: " + nameOrId);
        }
    }

    /**
     * DB에서 포켓몬 찾기 (@EntityGraph 사용으로 MultipleBagFetchException 해결)
     */
    private Pokemon findPokemonInDatabaseWithRelations(String nameOrId) {
        try {
            // 숫자면 ID로 조회
            long id = Long.parseLong(nameOrId);
            log.debug("ID로 포켓몬 조회 시도: {}", id);
            
            try {
                Optional<Pokemon> result = pokemonRepository.findByIdWithAllGraph(id);
                if (result.isPresent()) {
                    Pokemon pokemon = result.get();
                    log.debug("ID로 포켓몬 찾음: {}, 타입: {}, 특성: {}", 
                        pokemon.getName(), 
                        pokemon.getTypes() != null ? pokemon.getTypes().size() : "null",
                        pokemon.getAbilities() != null ? pokemon.getAbilities().size() : "null");
                    return pokemon;
                }
            } catch (Exception e) {
                log.error("EntityGraph로 ID 조회 실패, 기본 방식으로 시도: {}", e.getMessage());
                // EntityGraph 실패시 기본 방식으로 시도
                Optional<Pokemon> result = pokemonRepository.findById(id);
                if (result.isPresent()) {
                    Pokemon pokemon = result.get();
                    log.debug("기본 방식으로 ID 조회 성공: {}", pokemon.getName());
                    return pokemon;
                }
            }
            log.debug("ID {}로 포켓몬을 찾을 수 없습니다", id);
            return null;

        } catch (NumberFormatException e) {
            // 문자면 이름으로 조회
            log.debug("이름으로 포켓몬 조회 시도: {}", nameOrId);

            try {
                Optional<Pokemon> result = pokemonRepository.findByNameWithAllGraph(nameOrId);
                if (result.isPresent()) {
                    Pokemon pokemon = result.get();
                    log.debug("이름으로 포켓몬 찾음: {}, 타입: {}, 특성: {}",
                        pokemon.getName(),
                        pokemon.getTypes() != null ? pokemon.getTypes().size() : "null",
                        pokemon.getAbilities() != null ? pokemon.getAbilities().size() : "null");
                    return pokemon;
                }
            } catch (Exception ex) {
                log.error("EntityGraph로 이름 조회 실패, 기본 방식으로 시도: {}", ex.getMessage());
                // EntityGraph 실패시 기본 방식으로 시도
                Optional<Pokemon> result = pokemonRepository.findByName(nameOrId);
                if (!result.isPresent()) {
                    result = pokemonRepository.findByEnglishName(nameOrId);
                }
                if (result.isPresent()) {
                    Pokemon pokemon = result.get();
                    log.debug("기본 방식으로 이름 조회 성공: {}", pokemon.getName());
                    return pokemon;
                }
            }
            log.debug("이름 '{}'으로 포켓몬을 찾을 수 없습니다", nameOrId);
            return null;
        }
    }

    /**
     * 포켓몬 목록 조회 (타입, 특성 정보 포함)
     */
    @Cacheable(value = "pokemonList", key = "#limit + '::' + #offset")
    public PokemonListResponse getAllPokemon(int limit, int offset) {
        log.debug("포켓몬 목록 조회: limit={}, offset={}", limit, offset);

        try {
            Pageable pageable = PageRequest.of(offset / limit, limit);
            
            // EntityGraph를 사용하여 관련 정보 포함 조회
            Page<Pokemon> pokemonPage;
            try {
                pokemonPage = pokemonRepository.findAllWithAllGraph(pageable);
                log.debug("EntityGraph로 포켓몬 목록 조회 성공");
            } catch (Exception e) {
                log.warn("EntityGraph 조회 실패, 기본 방식으로 조회: {}", e.getMessage());
                pokemonPage = pokemonRepository.findAll(pageable);
            }

            // 요약 정보 생성
            List<PokemonSummary> summaries = pokemonPage.getContent().stream()
                    .map(this::convertToSummary)
                    .collect(Collectors.toList());

            // 상세 정보 생성 (타입, 특성 포함)
            List<PokemonResponse> detailedResults = pokemonPage.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            PokemonListResponse response = new PokemonListResponse();
            response.setCount((int) pokemonPage.getTotalElements());
            response.setResults(summaries);
            response.setDetailedResults(detailedResults); // 상세 정보 설정

            if (pokemonPage.hasNext()) {
                response.setNext("?limit=" + limit + "&offset=" + (offset + limit));
            }
            if (pokemonPage.hasPrevious()) {
                response.setPrevious("?limit=" + limit + "&offset=" + Math.max(0, offset - limit));
            }

            log.debug("포켓몬 목록 조회 완료: {}개 (상세 정보 포함)", detailedResults.size());
            return response;

        } catch (Exception e) {
            log.error("포켓몬 목록 조회 중 오류 발생", e);
            throw new RuntimeException("포켓몬 목록을 조회할 수 없습니다", e);
        }
    }

    /**
     * 타입별 포켓몬 조회
     */
    @Cacheable(value = "pokemonByType", key = "#typeName + '::' + #limit + '::' + #offset")
    public PokemonListResponse getPokemonByType(String typeName, int limit, int offset) {
        log.debug("타입별 포켓몬 조회: typeName={}, limit={}, offset={}", typeName, limit, offset);

        try {
            Pageable pageable = PageRequest.of(offset / limit, limit);
            Page<Pokemon> pokemonPage = pokemonRepository.findByTypeName(typeName, pageable);

            List<PokemonSummary> summaries = pokemonPage.getContent().stream()
                    .map(this::convertToSummary)
                    .collect(Collectors.toList());

            PokemonListResponse response = new PokemonListResponse();
            response.setCount((int) pokemonPage.getTotalElements());
            response.setResults(summaries);

            if (pokemonPage.hasNext()) {
                response.setNext("?limit=" + limit + "&offset=" + (offset + limit) + "&type=" + typeName);
            }
            if (pokemonPage.hasPrevious()) {
                response.setPrevious("?limit=" + limit + "&offset=" + Math.max(0, offset - limit) + "&type=" + typeName);
            }

            return response;

        } catch (Exception e) {
            log.error("타입별 포켓몬 조회 중 오류 발생: {}", typeName, e);
            throw new RuntimeException("타입별 포켓몬을 조회할 수 없습니다", e);
        }
    }

    /**
     * 특성별 포켓몬 조회
     */
    @Cacheable(value = "pokemonByAbility", key = "#abilityName + '::' + #limit + '::' + #offset")
    public PokemonListResponse getPokemonByAbility(String abilityName, int limit, int offset) {
        log.debug("특성별 포켓몬 조회: abilityName={}, limit={}, offset={}", abilityName, limit, offset);

        try {
            Pageable pageable = PageRequest.of(offset / limit, limit);
            Page<Pokemon> pokemonPage = pokemonRepository.findByAbilityName(abilityName, pageable);

            List<PokemonSummary> summaries = pokemonPage.getContent().stream()
                    .map(this::convertToSummary)
                    .collect(Collectors.toList());

            PokemonListResponse response = new PokemonListResponse();
            response.setCount((int) pokemonPage.getTotalElements());
            response.setResults(summaries);

            if (pokemonPage.hasNext()) {
                response.setNext("?limit=" + limit + "&offset=" + (offset + limit) + "&ability=" + abilityName);
            }
            if (pokemonPage.hasPrevious()) {
                response.setPrevious("?limit=" + limit + "&offset=" + Math.max(0, offset - limit) + "&ability=" + abilityName);
            }

            return response;

        } catch (Exception e) {
            log.error("특성별 포켓몬 조회 중 오류 발생: {}", abilityName, e);
            throw new RuntimeException("특성별 포켓몬을 조회할 수 없습니다", e);
        }
    }

    /**
     * 포켓몬 검색 기능 (타입, 특성 정보 포함)
     */
    // @Cacheable(value = "pokemonSearch", key = "#query") // 캐시 임시 비활성화
    public List<PokemonResponse> searchPokemon(String query) {
        log.debug("포켓몬 검색: {}", query);

        if (query == null || query.trim().isEmpty()) {
            throw new PokemonInvalidArgumentException("검색어를 입력해주세요");
        }

        try {
            // EntityGraph를 사용하여 연관 정보 포함 조회
            List<Pokemon> foundPokemon;
            try {
                foundPokemon = pokemonRepository.findAllWithAllGraph(PageRequest.of(0, 1000))
                        .getContent()
                        .stream()
                        .filter(pokemon ->
                                pokemon.getName().toLowerCase().contains(query.toLowerCase()) ||
                                        pokemon.getEnglishName().toLowerCase().contains(query.toLowerCase()))
                        .limit(20)
                        .collect(Collectors.toList());
                log.debug("EntityGraph로 검색 조회 성공");
            } catch (Exception e) {
                log.warn("EntityGraph 검색 실패, 기본 방식으로 검색: {}", e.getMessage());
                foundPokemon = pokemonRepository.findAll().stream()
                        .filter(pokemon ->
                                pokemon.getName().toLowerCase().contains(query.toLowerCase()) ||
                                        pokemon.getEnglishName().toLowerCase().contains(query.toLowerCase()))
                        .limit(20)
                        .collect(Collectors.toList());
            }

            // 상세 정보로 변환 (타입, 특성 포함)
            return foundPokemon.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("포켓몬 검색 중 오류 발생: {}", query, e);
            throw new RuntimeException("검색 중 오류가 발생했습니다", e);
        }
    }

    /**
     * API에서 포켓몬 로드 후 DB에 저장 (타입, 특성 포함)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Pokemon loadAndSavePokemonFromApi(String nameOrId) {
        try {
            // 1. API에서 기본 정보 조회
            PokemonResponse apiResponse = pokemonClient.getPokemon(nameOrId);
            if (apiResponse == null) {
                throw new PokemonNotFoundException("PokeAPI에서 포켓몬을 찾을 수 없습니다: " + nameOrId);
            }

            // 2. 동시성 문제 방지
            Pokemon existingPokemon = pokemonRepository.findById((long) apiResponse.getId()).orElse(null);
            if (existingPokemon != null) {
                return existingPokemon;
            }

            // 3. 종족 정보 조회 (한국어 이름용)
            PokemonSpeciesResponse speciesResponse = null;
            try {
                speciesResponse = pokemonClient.getPokeSpecies(nameOrId);
            } catch (Exception e) {
                log.warn("종족 정보 조회 실패 (계속 진행): {}", nameOrId);
            }

            // 4. Entity로 변환 (타입, 특성 포함)
            Pokemon pokemon = convertApiResponseToEntity(apiResponse, speciesResponse);

            // 5. DB에 저장
            Pokemon savedPokemon = pokemonRepository.save(pokemon);
            pokemonRepository.flush();

            log.info("포켓몬 저장 완료: {} (타입: {}개, 특성: {}개)",
                    savedPokemon.getName(),
                    savedPokemon.getTypes().size(),
                    savedPokemon.getAbilities().size());

            return savedPokemon;

        } catch (PokemonNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("포켓몬 저장 중 오류 발생: {}", nameOrId, e);
            throw new ExternalApiException("포켓몬 데이터를 저장할 수 없습니다: " + nameOrId, e);
        }
    }

    /**
     * API 응답을 Entity로 변환 (LazyInitializationException 해결)
     */
    private Pokemon convertApiResponseToEntity(PokemonResponse apiResponse, PokemonSpeciesResponse speciesResponse) {
        Pokemon pokemon = new Pokemon();

        // 기본 정보 설정
        pokemon.setId((long) apiResponse.getId());
        pokemon.setEnglishName(apiResponse.getName());
        pokemon.setHeight(apiResponse.getHeight());
        pokemon.setWeight(apiResponse.getWeight());

        // 한국어 이름 설정
        String koreanName = apiResponse.getName();
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

        // 타입 관계 설정 (직접 생성 방식)
        if (apiResponse.getTypes() != null) {
            for (com.pokeapi.model.Type typeSlot : apiResponse.getTypes()) {
                Type type = getOrCreateType(typeSlot.getType().getName());

                PokemonType pokemonType = new PokemonType();
                pokemonType.setPokemon(pokemon);
                pokemonType.setType(type);
                pokemonType.setSlot(typeSlot.getSlot());

                pokemon.getTypes().add(pokemonType);
                log.debug("타입 추가: {} (슬롯: {})", type.getName(), typeSlot.getSlot());
            }
        }

        // 특성 관계 설정 (직접 생성 방식)
        if (apiResponse.getAbilities() != null) {
            for (com.pokeapi.model.Ability abilitySlot : apiResponse.getAbilities()) {
                Ability ability = getOrCreateAbility(abilitySlot.getAbility().getName());

                PokemonAbility pokemonAbility = new PokemonAbility();
                pokemonAbility.setPokemon(pokemon);
                pokemonAbility.setAbility(ability);
                pokemonAbility.setSlot(abilitySlot.getSlot());
                pokemonAbility.setIsHidden(abilitySlot.isHidden());

                pokemon.getAbilities().add(pokemonAbility);
                log.debug("특성 추가: {} (슬롯: {}, 숨김: {})",
                        ability.getName(), abilitySlot.getSlot(), abilitySlot.isHidden());
            }
        }

        return pokemon;
    }

    /**
     * 타입 조회 또는 생성
     */
    private Type getOrCreateType(String name) {
        return typeRepository.findByName(name)
                .orElseGet(() -> {
                    Type newType = new Type();
                    newType.setName(name);
                    newType.setKoreanName(translateTypeToKorean(name));
                    return typeRepository.save(newType);
                });
    }

    /**
     * 특성 조회 또는 생성
     */
    private Ability getOrCreateAbility(String name) {
        return abilityRepository.findByName(name)
                .orElseGet(() -> {
                    Ability newAbility = new Ability();
                    newAbility.setName(name);
                    newAbility.setKoreanName(translateAbilityToKorean(name));
                    return abilityRepository.save(newAbility);
                });
    }

    /**
     * 타입 영어 → 한국어 번역
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
     * 특성 영어 → 한국어 번역
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
            default: return englishName;
        }
    }

    /**
     * Entity를 Response DTO로 변환 (수정된 버전)
     */
    private PokemonResponse convertToResponse(Pokemon pokemon) {
        PokemonResponse response = new PokemonResponse();

        response.setId(pokemon.getId().intValue());
        response.setName(pokemon.getName());
        response.setEnglishName(pokemon.getEnglishName());
        response.setHeight(pokemon.getHeight());
        response.setWeight(pokemon.getWeight());

        if (pokemon.getSpriteUrl() != null) {
            PokemonResponse.Sprites sprites = new PokemonResponse.Sprites();
            sprites.setFront_default(pokemon.getSpriteUrl());
            response.setSprites(sprites);
        }

        try {
            // 타입 정보 변환 (null 체크 추가)
            if (pokemon.getTypes() != null && !pokemon.getTypes().isEmpty()) {
                List<com.pokeapi.model.Type> types = pokemon.getTypes().stream()
                        .sorted(Comparator.comparing(pt -> pt.getSlot()))
                        .map(pt -> {
                            com.pokeapi.model.Type typeDto = new com.pokeapi.model.Type();
                            typeDto.setSlot(pt.getSlot());

                            com.pokeapi.model.Type.TypeDetail typeDetail = new com.pokeapi.model.Type.TypeDetail();
                            typeDetail.setName(pt.getType().getName());
                            typeDetail.setUrl("");
                            typeDto.setType(typeDetail);

                            return typeDto;
                        })
                        .collect(Collectors.toList());
                response.setTypes(types);
            } else {
                log.warn("포켓몬 {}의 타입 정보가 비어있습니다", pokemon.getName());
                response.setTypes(null);
            }

            // 특성 정보 변환 (null 체크 추가)
            if (pokemon.getAbilities() != null && !pokemon.getAbilities().isEmpty()) {
                List<com.pokeapi.model.Ability> abilities = pokemon.getAbilities().stream()
                        .sorted(Comparator.comparing(pa -> pa.getSlot()))
                        .map(pa -> {
                            com.pokeapi.model.Ability abilityDto = new com.pokeapi.model.Ability();
                            abilityDto.setSlot(pa.getSlot());
                            abilityDto.setHidden(pa.getIsHidden());

                            com.pokeapi.model.Ability.AbilityDetail abilityDetail = new com.pokeapi.model.Ability.AbilityDetail();
                            abilityDetail.setName(pa.getAbility().getName());
                            abilityDetail.setUrl("");
                            abilityDto.setAbility(abilityDetail);

                            return abilityDto;
                        })
                        .collect(Collectors.toList());
                response.setAbilities(abilities);
            } else {
                log.warn("포켓몬 {}의 특성 정보가 비어있습니다", pokemon.getName());
                response.setAbilities(null);
            }

        } catch (Exception e) {
            log.error("DTO 변환 중 오류 발생: {}", pokemon.getName(), e);
            response.setTypes(null);
            response.setAbilities(null);
        }

        return response;
    }

    /**
     * Entity를 Summary DTO로 변환
     */
    private PokemonSummary convertToSummary(Pokemon pokemon) {
        PokemonSummary summary = new PokemonSummary();
        summary.setName(pokemon.getName());
        summary.setUrl("/api/pokemon/" + pokemon.getId());
        return summary;
    }
}