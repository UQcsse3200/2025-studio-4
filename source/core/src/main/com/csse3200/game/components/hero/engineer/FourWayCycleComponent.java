package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * A component that cycles the firing direction of an attached {@link TurretAttackComponent}
 * in the fixed order: Up → Right → Down → Left.
 * <p>
 * This component does NOT rotate the sprite; it only updates the bullet firing direction.
 * Used for Engineer’s automated turrets that continuously shoot in four directions.
 * </p>
 */
public class FourWayCycleComponent extends Component {
    /**
     * Time interval (in seconds) between direction switches.
     */
    private final float switchIntervalSec;

    /**
     * Timer to track when to change directions.
     */
    private float timer = 0f;

    /**
     * Predefined four cardinal directions (Up, Right, Down, Left).
     */
    private static final Vector2[] DIRS = {
            new Vector2(0, 1),   // Up
            new Vector2(1, 0),   // Right
            new Vector2(0, -1),  // Down
            new Vector2(-1, 0)   // Left
    };

    /**
     * Index of the current direction in the DIRS array.
     */
    private int idx;

    /**
     * Constructs a Four-Way Direction Cycle component.
     * Starts cycling from the direction that most closely matches the given initial vector.
     *
     * @param switchIntervalSec Interval (in seconds) between direction changes.
     * @param initialDir        The initial facing direction. The nearest of the four
     *                          cardinal directions will be selected as the starting point.
     */
    public FourWayCycleComponent(float switchIntervalSec, Vector2 initialDir) {
        this.switchIntervalSec = switchIntervalSec;
        this.idx = pickNearestIndex(initialDir);
    }

    @Override
    public void create() {
        // On creation, set the turret's initial firing direction
        var atk = entity.getComponent(TurretAttackComponent.class);
        if (atk != null) atk.setDirection(DIRS[idx]);
    }

    @Override
    public void update() {
        // Accumulate elapsed time
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer += dt;

        // Wait until the interval has passed before switching direction
        if (timer < switchIntervalSec) return;

        // Reset timer and move to the next direction cyclically
        timer = 0f;
        idx = (idx + 1) % DIRS.length;

        // Update the direction of the turret’s attack component
        var atk = entity.getComponent(TurretAttackComponent.class);
        if (atk != null) atk.setDirection(DIRS[idx]);
    }

    /**
     * Finds the direction index (0–3) that is closest to the given vector.
     * This ensures the turret starts facing roughly the same way as {@code initialDir}.
     *
     * @param v The vector to compare against (e.g., initial direction).
     * @return The index (0–3) of the closest cardinal direction.
     */
    private static int pickNearestIndex(Vector2 v) {
        if (v == null) return 0;

        // Normalize input vector to compare direction only
        Vector2 n = new Vector2(v).nor();
        int best = 0;
        float bestDot = -Float.MAX_VALUE;

        // Choose the direction with the highest dot product (closest alignment)
        for (int i = 0; i < DIRS.length; i++) {
            float d = n.dot(DIRS[i]);
            if (d > bestDot) {
                bestDot = d;
                best = i;
            }
        }

        // Return the most similar direction as the starting point
        return best;
    }
}
