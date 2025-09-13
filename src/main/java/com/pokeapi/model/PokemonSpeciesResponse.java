package com.pokeapi.model;

import java.io.Serializable;
import java.util.List;

public class PokemonSpeciesResponse implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID

    private List<Name> names;

    public List<Name> getNames() {
        return names;
    }

    public void setNames(List<Name> names) {
        this.names = names;
    }

    public static class Name implements Serializable {
        private static final long serialVersionUID = 1L; // 직렬화 버전 ID

        private String name;

        private Language language;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Language getLanguage() {
            return language;
        }

        public void setLanguage(Language language) {
            this.language = language;
        }
    }

    public static class Language implements Serializable {
        private static final long serialVersionUID = 1L; // 직렬화 버전 ID

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
