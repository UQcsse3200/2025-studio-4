package com.csse3200.game.components;

/**
 * Holds the player's points and notifies the HUD on change.
 */
public class PlayerScoreComponent extends Component {
    private int totalScore;

    public int getTotalScore() { return this.totalScore; }

    /**
     * Adds a non-negative amount of points to the total score and notifies the HUD on change.
     */
    public void addPoints(int points) {
        if (points <= 0) {          // only positive points allowed
            return;
        }
        totalScore += points;
        if (entity != null) {
            entity.getEvents().trigger("updateScore", totalScore);
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
