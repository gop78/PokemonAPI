package com.pokeapi.model;

import java.io.Serializable;

public class PokemonSummary implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID

    private String name;

    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private int getId() {
        String[] patrs = url.split("/");
        return Integer.parseInt(patrs[patrs.length - 1]);
    }
}
