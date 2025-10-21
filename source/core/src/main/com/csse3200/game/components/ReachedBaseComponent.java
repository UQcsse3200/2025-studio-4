package com.csse3200.game.components;

/**
 * Marks an enemy that reached and attacked the player's base.
 * Used to prevent currency drops for enemies that weren't killed by the player.
 */
public class ReachedBaseComponent extends Component {
    private boolean reachedBase = false;

    /**
     * Mark that this enemy has reached the base.
     */
    public void markAsReachedBase() {
        this.reachedBase = true;
    }

    /**
     * Check if this enemy reached the base.
     */
    public boolean hasReachedBase() {
        return reachedBase;
    }
}