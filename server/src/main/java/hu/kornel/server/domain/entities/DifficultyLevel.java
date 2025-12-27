package hu.kornel.server.domain.entities;

public enum DifficultyLevel {
    EASY,
    MEDIUM,
    HARD;

    public boolean canIncrease() {
        return this != HARD;
    }

    public boolean canDecrease() {
        return this != EASY;
    }

    public DifficultyLevel increase() {
        return switch (this) {
            case EASY -> MEDIUM;
            case MEDIUM -> HARD;
            case HARD -> HARD;
        };
    }

    public DifficultyLevel decrease() {
        return switch (this) {
            case EASY -> EASY;
            case MEDIUM -> EASY;
            case HARD -> MEDIUM;
        };
    }
}