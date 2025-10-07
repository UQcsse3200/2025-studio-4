package com.csse3200.game.components.towers;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csse3200.game.components.Component;

/**
 * Component to make an entity orbit around a target entity.
 */
public class OrbitComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(OrbitComponent.class);

    private final Entity target;
    private float radius;
    private final float speed;
    private float angle;

    public OrbitComponent(Entity target, float radius, float speed) {
        this.target = target;
        this.radius = radius;
        this.speed = speed;
        this.angle = 0f;
    }

    @Override
    public void create() {
        if (entity == null) return;
        if (target == null) {
            logger.error("OrbitComponent.create(): target is null, skipping setup.");
            return;
        }

        // Obtain a reliable center for target (preferred) or fallback to position
        Vector2 targetCenter = target.getCenterPosition();
        if (targetCenter == null) {
            targetCenter = target.getPosition();
            if (targetCenter == null) {
                logger.error("OrbitComponent.create(): target has no position, skipping setup.");
                return;
            }
        }

        // Sanitize radius (fallback to a small positive radius if invalid)
        if (!isFinite(this.radius) || this.radius <= 0f) {
            logger.warn("OrbitComponent.create(): invalid radius {}, clamping to 0.5", this.radius);
            this.radius = 0.5f;
        }

        // Use entity center if available to compute the initial angle so orbit is centred
        Vector2 myCenter = entity.getCenterPosition();
        if (myCenter == null) {
            myCenter = entity.getPosition();
        }
        if (myCenter == null) {
            this.angle = 0f;
        } else {
            float dx = myCenter.x - targetCenter.x;
            float dy = myCenter.y - targetCenter.y;
            if (dx == 0f && dy == 0f) {
                this.angle = 0f;
            } else {
                this.angle = (float) Math.atan2(dy, dx);
            }
        }

        // Snap the head exactly to the circumference around the target center,
        // compensating for the head entity's center offset (so visual center lies on circumference).
        Vector2 headCenter = entity.getCenterPosition();
        Vector2 headPos = entity.getPosition();
        Vector2 centerOffset = new Vector2(0f, 0f);
        if (headCenter != null && headPos != null) {
            centerOffset = headCenter.cpy().sub(headPos);
        }

        float x = targetCenter.x + this.radius * (float) Math.cos(this.angle);
        float y = targetCenter.y + this.radius * (float) Math.sin(this.angle);
        entity.setPosition(x - centerOffset.x, y - centerOffset.y);
    }

    @Override
    public void update() {
        if (entity == null) return;
        if (target == null) {
            logger.error("OrbitComponent: target is null, skipping update to avoid native crash.");
            return;
        }
        if (!target.isActive()) {
            logger.error("OrbitComponent: target entity is not active, skipping update to avoid native crash.");
            return;
        }

        float delta = com.badlogic.gdx.Gdx.graphics != null ? com.badlogic.gdx.Gdx.graphics.getDeltaTime() : 1f / 60f;

        // Interpret `speed` as angular velocity in radians/sec
        angle += speed * delta;

        Vector2 targetCenter = target.getCenterPosition();
        if (targetCenter == null) {
            targetCenter = target.getPosition();
            if (targetCenter == null) {
                logger.error("OrbitComponent: target position is null, skipping update.");
                return;
            }
        }

        // ensure radius remains finite and positive
        float r = isFinite(this.radius) && this.radius > 0f ? this.radius : 0.5f;

        // Compensate for head visual center offset so the visual center orbits the true circumference.
        Vector2 headCenter = entity.getCenterPosition();
        Vector2 headPos = entity.getPosition();
        Vector2 centerOffset = new Vector2(0f, 0f);
        if (headCenter != null && headPos != null) {
            centerOffset = headCenter.cpy().sub(headPos);
        }

        float x = targetCenter.x + r * (float) Math.cos(angle);
        float y = targetCenter.y + r * (float) Math.sin(angle);
        entity.setPosition(x - centerOffset.x, y - centerOffset.y);
    }

    /**
     * Update the orbit radius at runtime. Invalid values are ignored or clamped.
     *
     * @param r new radius to set (world units)
     */
    public void setRadius(float r) {
        if (!isFinite(r) || r <= 0f) {
            logger.warn("OrbitComponent.setRadius(): invalid radius {}, ignoring.", r);
            return;
        }
        this.radius = r;
    }

    /** Returns the current orbit radius. */
    public float getRadius() {
        return this.radius;
    }

    // small helper
    private static boolean isFinite(float v) {
        return !Float.isNaN(v) && !Float.isInfinite(v);
    }
}