package com.csse3200.game.components.movement;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;

import java.util.ArrayList;
import java.util.List;


public class AdjustSpeedByHealthComponent extends Component {

    // Dependencies on the one enemy
    private CombatStatsComponent statsComponent;             // used to access health
    private PhysicsMovementComponent movementComponent;      // used to access speed

    // Baseline values for health and speed
    private float initialHealth;
    private float initialSpeed;

    // List to store health percentage values and associated speed settings
    private final List<HealthSpeedValues> healthSpeedThresholds = new ArrayList<>();

    // Add a
    public AdjustSpeedByHealthComponent addThreshold(float healthPercent, float newSpeed) {
        healthSpeedThresholds.add(new HealthSpeedValues(healthPercent, newSpeed));
        return this;
    }

    /**
     * Determine the initial health and speed upon creation.
     */
    @Override
    public void create() {
        statsComponent = entity.getComponent(CombatStatsComponent.class);
        movementComponent = entity.getComponent(PhysicsMovementComponent.class);

        initialHealth = statsComponent.getHealth();
        initialSpeed = movementComponent.maxSpeed.x;            // X and Y speeds are set equally
    }

    /**
     * Set a new speed if the health lowers to a certain percentage of the original value.
     */
    @Override
    public void update() {
        float currentHealth = statsComponent.getHealth();
        float currentHealthPercent = currentHealth / initialHealth;
        float updatedSpeed = initialSpeed;          // Default to initial speed

        // Check if health <= any thresholds. Given list in ascending health % order
        for (int i = 0; i < healthSpeedThresholds.size(); i++) {
            HealthSpeedValues valuePair = healthSpeedThresholds.get(i);
            if (currentHealthPercent <= valuePair.percent) {
                updatedSpeed = valuePair.speed;
                break;
            }
        }

        // update entity speeds for X and Y axes
        movementComponent.maxSpeed.set(updatedSpeed, updatedSpeed);
    }
}

