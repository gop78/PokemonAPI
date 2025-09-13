package com.pokeapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 포켓몬을 찾을 수 없는 경우 (404)
     */
    @ExceptionHandler(PokemonNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePokemonNotFoundException(PokemonNotFoundException e) {
        log.warn("포켓몬을 찾을 수 없음: {}" , e.getMessage());

        Map<String, Object> response = createErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Pokemon Not Found",
                e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 외부 API 호출 실패 (503)
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Map<String, Object>> handleExternalApiException(ExternalApiException e) {
        log.error("외부 API 호출 실패: {}", e.getMessage(), e);

        Map<String, Object> response = createErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "External Service Error",
                "포켓몬 정보를 가져올 수 없습니다. 잠시 후 다시 시도해주세요."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 데이터 로드 실패 (500)
     */
    @ExceptionHandler(DataLoadException.class)
    public ResponseEntity<Map<String, Object>> handleDataLoadException(DataLoadException e) {
        log.error("데이터 로드 실패: {}", e.getMessage(), e);

        Map<String, Object> response = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Data Load Error",
                "데이터를 로드하는 중 오류가 발생했습니다."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 일반적인 런타임 예외 (500)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("예상치 못한 오류: {}", e.getMessage(), e);

        Map<String, Object> response = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "서버에서 오류가 발생했습니다."
        );

        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 입력 오류 예외 (400)
     * @param e
     * @return
     */
    @ExceptionHandler(PokemonInvalidArgumentException.class)
    public ResponseEntity<Map<String, Object>> handlePokemonInvalidArgumentException(PokemonInvalidArgumentException e) {
        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 공통 에러 응답 생성
     * @param status
     * @param error
     * @param message
     * @return
     */
    private Map<String, Object> createErrorResponse(int status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status);
        response.put("error", error);
        response.put("message", message);
        return response;
    }
}
