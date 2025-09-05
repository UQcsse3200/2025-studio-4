package com.csse3200.game.components.player;

public class PlayerRankingComponent {
    public int stageCompleted = 0;
    public int enemiesKilled = 0;
    public int remainingHealth = 0;
    public int towersLeft = 0;
    public float timeElapsed = 0f;
    public boolean noDamageClear = false;

    public void reset() {
        stageCompleted = 0;
        enemiesKilled = 0;
        remainingHealth = 0;
        towersLeft = 0;
        timeElapsed = 0f;
        noDamageClear = false;
    }
}
