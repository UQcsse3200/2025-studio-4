package com.csse3200.game.components;

/**
 * Holds the player's points and notifies the HUD on change.
 */
public class PlayerScoreComponent extends Component {
    private int totalScore;

    public int getTotalScore() { return this.totalScore; }

    /**
     * Adds points and notifies HUD via a player event (like updateHealth/updateScrap).
     */
    public void addPoints(int points) {
        totalScore += points;
        if (entity != null) {
            entity.getEvents().trigger("updateScore", points); // <- same style as updateHealth/updateScrap
        }
    }

    /** Resets points to 0 and notifies HUD. */
    public void reset() {
        totalScore = 0;
        if (entity != null) {
            entity.getEvents().trigger("updateScore", totalScore);
        }
    }



}
