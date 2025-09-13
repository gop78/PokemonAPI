package com.pokeapi.model;

import java.io.Serializable;

public class Type implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID

    private int slot;

    private TypeDetail type;

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public TypeDetail getType() {
        return type;
    }

    public void setType(TypeDetail type) {
        this.type = type;
    }

    public static class TypeDetail implements Serializable {
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
    }
}
