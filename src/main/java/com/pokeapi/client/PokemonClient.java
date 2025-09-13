package com.pokeapi.client;

import com.pokeapi.model.PokemonListResponse;
import com.pokeapi.model.PokemonResponse;
import com.pokeapi.model.PokemonSpeciesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "pokeApi", url = "https://pokeapi.co/api/v2")
public interface PokemonClient {

    @GetMapping("/pokemon/{name}")
    PokemonResponse getPokemon(@PathVariable("name") String name);

    @GetMapping("/pokemon")
    PokemonListResponse getAllPokemon(@RequestParam("limit") int limit, @RequestParam("offset") int offset);

    @GetMapping("/pokemon-species/{name}")
    PokemonSpeciesResponse getPokeSpecies(@PathVariable("name") String name);
}
