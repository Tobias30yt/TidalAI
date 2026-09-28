package com.tidalai.models;

public class AIModel {

    private final String id;
    private final String name;

    private boolean loaded;

    public AIModel(
        String id,
        String name
    ) {
        this.id = id;
        this.name = name;
        this.loaded = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public String toString() {
        return name;
    }
}
