package com.pokeapi.repository;

import com.pokeapi.entity.PokemonAbility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PokemonAbilityRepository extends JpaRepository<PokemonAbility, Long> {
}
