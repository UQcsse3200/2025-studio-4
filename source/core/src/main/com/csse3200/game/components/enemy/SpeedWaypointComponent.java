package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;

/**
 * Marks a waypoint with a speed multiplier so enemies can accelerate or decelerate
 * when they reach this point along their path.
 */
public class SpeedWaypointComponent extends Component {
    private final float speedMultiplier;

    public SpeedWaypointComponent(float speedMultiplier) {
        if (speedMultiplier <= 0f) {
            throw new IllegalArgumentException("Speed multiplier must be positive");
        }
        this.speedMultiplier = speedMultiplier;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }
}
