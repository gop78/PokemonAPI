package com.pokeapi.model;

import java.io.Serializable;
import java.util.List;

public class PokemonListResponse implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID
    private int count;
    private String next;
    private String previous;
    private List<PokemonSummary> results;
    private List<PokemonResponse> detailedResults;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public List<PokemonSummary> getResults() {
        return results;
    }

    public void setResults(List<PokemonSummary> results) {
        this.results = results;
    }

    public List<PokemonResponse> getDetailedResults() {
        return detailedResults;
    }

    public void setDetailedResults(List<PokemonResponse> detailedResults) {
        this.detailedResults = detailedResults;
    }
}
