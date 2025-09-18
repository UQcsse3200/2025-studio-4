package com.csse3200.game.components;

/**
 * Holds the player's points and notifies the HUD on change.
 */
public class PlayerScoreComponent extends Component {
    private int totalScore;

    public int getTotalScore() { return this.totalScore; }

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
        if (entity != null) {
            entity.getEvents().trigger("updateScore", totalScore);
        }
    }



}
