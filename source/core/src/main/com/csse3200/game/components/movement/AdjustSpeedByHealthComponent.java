package com.csse3200.game.components.movement;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.entities.factories.GruntEnemyFactory;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import java.util.ArrayList;
import java.util.List;

public class AdjustSpeedByHealthComponent extends Component {

    private CombatStatsComponent statsComponent;
    private PhysicsMovementComponent movementComponent;
    private WaypointComponent waypointComponent;
    private float initialHealth;
    private Float initialSpeed;
    private float lastAppliedSpeed;
    private final List<HealthSpeedValues> healthSpeedThresholds = new ArrayList<>();

    public AdjustSpeedByHealthComponent addThreshold(float healthPercent, float newSpeed) {
        healthSpeedThresholds.add(new HealthSpeedValues(healthPercent, newSpeed));
        return this;
    }

    @Override
    public void create() {
        statsComponent = entity.getComponent(CombatStatsComponent.class);
        movementComponent = entity.getComponent(PhysicsMovementComponent.class);
        waypointComponent = entity.getComponent(WaypointComponent.class);
        initialHealth = statsComponent.getHealth();
    }

    @Override
    public void update() {
        // Capture current and previous speeds from the waypoint component
        if (initialSpeed == null && waypointComponent != null) {
            Vector2 wpVector = waypointComponent.getSpeed();
            if (wpVector != null) {
                initialSpeed = wpVector.x;
                lastAppliedSpeed = initialSpeed; // mark initial speed as applied
            }
        }
        // Avoid NPE, still waiting for initial speed
        if (initialSpeed == null) {
            return;
        }

        // Find target speed based on health thresholds
        float healthPercent = statsComponent.getHealth() / initialHealth;
        float targetSpeed = initialSpeed; // default to baseline if no threshold matched

        // Assume thresholds are in ascending order of health percentages in GruntEnemyFactory
        for (HealthSpeedValues h : healthSpeedThresholds) {
            if (healthPercent <= h.percent) {
                targetSpeed = h.speed;
                break;
            }
        }

        // Only apply if the speed actually changes
        if (!MathUtils.isEqual(lastAppliedSpeed, targetSpeed)) {
            Vector2 newSpeed = new Vector2(targetSpeed, targetSpeed);
            if (waypointComponent != null) {
                GruntEnemyFactory.updateSpeed(entity, newSpeed);
            } else if (movementComponent != null) { // fallback path
                movementComponent.maxSpeed.set(newSpeed);
                movementComponent.setSpeed(newSpeed);
            }
            lastAppliedSpeed = targetSpeed;
        }
    }
}
