package com.pokeapi.exception;

public class PokemonNotFoundException extends RuntimeException {

    public PokemonNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PokemonNotFoundException(Long id) {
        super("포켓몬을 찾을 수 없습니다 (ID: " + id + ")");
    }

    public PokemonNotFoundException(String name) {
        super("포켓몬을 찾을 수 없습니다 (이름: " + name + ")");
    }
}
