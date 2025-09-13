package com.pokeapi.exception;

public class PokemonInvalidArgumentException extends RuntimeException {
    public PokemonInvalidArgumentException(String message) {
        super(message);
    }
    public PokemonInvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
