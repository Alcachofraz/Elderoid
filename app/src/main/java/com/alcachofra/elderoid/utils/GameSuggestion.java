package com.alcachofra.elderoid.utils;

import java.util.Objects;

public class GameSuggestion implements Comparable<GameSuggestion> {
    private final String name;
    private final String path;
    private final String imagePath;
    private final String description;

    @Override
    public int compareTo(GameSuggestion o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameSuggestion newGame = (GameSuggestion) o;
        return Objects.equals(name, newGame.name) &&
                Objects.equals(path, newGame.path) &&
                Objects.equals(imagePath, newGame.imagePath) &&
                Objects.equals(description, newGame.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, imagePath, description);
    }

    @Override
    public String toString() {
        return "NewGame{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public GameSuggestion(String name, String path, String imagePath, String description) {
        this.name = name;
        this.path = path;
        this.imagePath = imagePath;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDescription() {
        return description;
    }
}
