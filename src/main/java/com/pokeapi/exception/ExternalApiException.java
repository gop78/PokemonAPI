package com.pokeapi.exception;

/**
 * 외부 API 호출 중 발생하는 예외
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String api, Throwable cause) {
        super("외부 API 호출 실패: " + api, cause);
    }
}
