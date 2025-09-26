package com.csse3200.game.components;

/**
 * Holds the player's game statistics and notifies the HUD on change.
 */
public class PlayerScoreComponent extends Component {
    private int totalScore;
    private int level;
    private int enemiesKilled;
    private long gameStartTime;
    private long gameDuration; // in milliseconds
    private int wavesSurvived;

    public PlayerScoreComponent() {
        this.gameStartTime = System.currentTimeMillis();
        this.level = 1;
        this.enemiesKilled = 0;
        this.wavesSurvived = 0;
        this.gameDuration = 0;
    }

    public int getTotalScore() { return this.totalScore; }
    public int getLevel() { return this.level; }
    public int getEnemiesKilled() { return this.enemiesKilled; }
    public long getGameDuration() { return this.gameDuration; }
    public int getWavesSurvived() { return this.wavesSurvived; }

    /**
     * Adds points to the total score and notifies the HUD on change.
     * Only positive points can be added (negative points are ignored).
     */
    public void addPoints(int points) {
        if (points > 0) {
            totalScore += points;
            if (entity != null) {
                entity.getEvents().trigger("updateScore", totalScore);
            }
        }
    }

    /** Resets points to 0 and notifies HUD. */
    public void reset() {
        totalScore = 0;
        level = 1;
        enemiesKilled = 0;
        wavesSurvived = 0;
        gameStartTime = System.currentTimeMillis();
        gameDuration = 0;
        if (entity != null) {
            entity.getEvents().trigger("updateScore", totalScore);
        }
    }

    /**
     * Increments the enemy kill count
     */
    public void addEnemyKill() {
        enemiesKilled++;
        if (entity != null) {
            entity.getEvents().trigger("updateEnemyKills", enemiesKilled);
        }
    }

    /**
     * Sets the current level
     */
    public void setLevel(int level) {
        this.level = Math.max(1, level);
        if (entity != null) {
            entity.getEvents().trigger("updateLevel", this.level);
        }
    }

    /**
     * Increments the waves survived count
     */
    public void addWaveSurvived() {
        wavesSurvived++;
        if (entity != null) {
            entity.getEvents().trigger("updateWaves", wavesSurvived);
        }
    }

    /**
     * Updates the game duration based on current time
     */
    public void updateGameDuration() {
        gameDuration = System.currentTimeMillis() - gameStartTime;
    }

    /**
     * Gets formatted game duration as a string (MM:SS)
     */
    public String getFormattedGameDuration() {
        updateGameDuration();
        long seconds = gameDuration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }



}
