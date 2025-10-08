package com.csse3200.game.components.movement;

import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that gradually increases an enemy's speed over time.
 * Creates urgency for the player to eliminate the enemy quickly.
 */
public class AccelerateOverTimeComponent extends Component {

    private PhysicsMovementComponent movementComponent;

    private final float initialSpeed;
    private final float maxSpeed;
    private final float accelerationRate; // Speed increase per second
    private float currentSpeed;
    private float timeAlive = 0f;

    /**
     * Creates an acceleration component.
     *
     * @param initialSpeed Starting speed of the enemy
     * @param maxSpeed Maximum speed the enemy can reach
     * @param accelerationRate How much speed increases per second
     */
    public AccelerateOverTimeComponent(float initialSpeed, float maxSpeed, float accelerationRate) {
        this.initialSpeed = initialSpeed;
        this.maxSpeed = maxSpeed;
        this.accelerationRate = accelerationRate;
        this.currentSpeed = initialSpeed;
    }

    @Override
    public void create() {
        movementComponent = entity.getComponent(PhysicsMovementComponent.class);
        if (movementComponent != null) {
            movementComponent.maxSpeed.set(initialSpeed, initialSpeed);
        }
    }

    @Override
    public void update() {
        if (movementComponent == null) {
            return;
        }

        // Account for time scale (game speed)
        float deltaTime = ServiceLocator.getTimeSource().getDeltaTime();
        timeAlive += deltaTime;

        // Calculate new speed based on acceleration
        currentSpeed = Math.min(initialSpeed + (accelerationRate * timeAlive), maxSpeed);

        // Update the entity's movement speed
        movementComponent.maxSpeed.set(currentSpeed, currentSpeed);
    }

    /**
     * Get the current speed of the enemy.
     * @return current speed value
     */
    public float getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     * Get how long the enemy has been alive.
     * @return time in seconds
     */
    public float getTimeAlive() {
        return timeAlive;
    }
}

