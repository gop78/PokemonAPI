package com.pokeapi.model;

import java.io.Serializable;

public class Ability implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID
    private AbilityDetail ability;
    private boolean isHidden;
    private int slot;

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public AbilityDetail getAbility() {
        return ability;
    }

    public void setAbility(AbilityDetail ability) {
        this.ability = ability;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public static class AbilityDetail implements Serializable {
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
