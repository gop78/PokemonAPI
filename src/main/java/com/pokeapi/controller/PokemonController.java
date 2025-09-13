package com.pokeapi.controller;

import com.pokeapi.exception.PokemonInvalidArgumentException;
import com.pokeapi.model.PokemonListResponse;
import com.pokeapi.model.PokemonResponse;
import com.pokeapi.service.PokemonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private static final Logger log = LoggerFactory.getLogger(PokemonController.class);

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    /**
     * 개뱔 포켓몬 조회 (이름 또는 ID)
     * @param nameOrId
     * @return
     */
    @GetMapping("/{nameOrId}")
    public ResponseEntity<PokemonResponse> getPokemon(@PathVariable String nameOrId) {
        log.debug("포켓몬 조회 요청: {}", nameOrId);
        // GlobalWExceptionHandler로 비즈니스 로직에만 집중
        return ResponseEntity.ok(pokemonService.getPokemon(nameOrId));
    }

    /**
     * 포켓몬 목록 조회 (페이징)
     * @param limit
     * @param offset
     * @return
     */
    @GetMapping()
    public ResponseEntity<PokemonListResponse> getAllPokemon(@RequestParam(defaultValue = "20") int limit,
                                             @RequestParam(defaultValue = "0") int offset) {

        log.debug("포켓몬 목록 조회 요청: limit: {}, offset: {}", limit, offset);

        // 유효성 검증
        if (limit < 1 || limit > 100) {
            throw new PokemonInvalidArgumentException("limit은 1-100 사이의 값이어야 합니다.");
        }

        try {
            PokemonListResponse response = pokemonService.getAllPokemon(limit, offset);
            log.debug("포켓몬 목록 조회 성공: {}개 반환", response.getDetailedResults().size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("포켓몬 목록 조회 실패: limit={}, offset={}", limit, offset, e);
            throw e;
        }
    }

    /**
     * 포켓몬 검색 (타입, 특성 정보 포함)
     * @param q
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<List<PokemonResponse>> searchPokemon(@RequestParam String q) {
        return ResponseEntity.ok(pokemonService.searchPokemon(q));
    }

    /**
     * 디버깅용: 특정 포켓몬의 연관 데이터 상태 확인
     */
    @GetMapping("/debug/{nameOrId}")
    public ResponseEntity<Map<String, Object>> debugPokemon(@PathVariable String nameOrId) {
        log.debug("포켓몬 디버그 요청: {}", nameOrId);
        
        Map<String, Object> debug = new HashMap<>();
        
        try {
            // 직접 Service 메서드 호출해서 결과 확인
            PokemonResponse response = pokemonService.getPokemon(nameOrId);
            
            debug.put("serviceResponse", "success");
            debug.put("responseId", response.getId());
            debug.put("responseName", response.getName());
            debug.put("responseEnglishName", response.getEnglishName());
            debug.put("responseTypesNull", response.getTypes() == null);
            debug.put("responseAbilitiesNull", response.getAbilities() == null);
            
            if (response.getTypes() != null) {
                debug.put("responseTypesCount", response.getTypes().size());
            }
            
            if (response.getAbilities() != null) {
                debug.put("responseAbilitiesCount", response.getAbilities().size());
            }
            
            return ResponseEntity.ok(debug);
            
        } catch (Exception e) {
            debug.put("serviceError", e.getMessage());
            debug.put("serviceErrorType", e.getClass().getSimpleName());
            return ResponseEntity.ok(debug);
        }
    }
}
