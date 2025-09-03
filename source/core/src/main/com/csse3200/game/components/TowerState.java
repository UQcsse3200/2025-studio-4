package com.csse3200.game.components;

/**
 * Enum representing the possible states of a tower.
 */
public enum TowerState {
    /** Tower is idle and not targeting any enemy. */
    IDLE,
    /** Tower is targeting an enemy. */
    TARGETING,
    /** Tower is actively attacking an enemy. */
    ATTACKING
}