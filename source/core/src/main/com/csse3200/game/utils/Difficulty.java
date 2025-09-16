package com.csse3200.game.utils;

public enum Difficulty {
    EASY(1),
    MEDIUM(2),
    HARD(4);

    private final int multiplier;

    Difficulty(int multiplier) {
        this.multiplier = multiplier;
    }

    public int getMultiplier() {
        return multiplier;
    }
}
