package com.csse3200.game.components.rewards;

public class Reward {
    private String name;
    private String description;
    private int gold;

    public Reward(String name, String description, int gold) {
        this.name = name;
        this.description = description;
        this.gold = gold;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getGold() {
        return gold;
    }

    @Override
    public String toString() {
        return name + " - " + description + " (+" + gold + " gold)";
    }
}
