package com.pokeapi;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TestPokemonRepository extends JpaRepository<TestPokemon, Long> {
}
