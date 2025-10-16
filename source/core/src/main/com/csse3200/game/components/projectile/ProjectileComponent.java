package com.csse3200.game.components.projectile;

import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Projectile component:
 * <p>
 * Handles projectile velocity and lifetime. When the lifetime expires,
 * the projectile is safely despawned. Compatible with pooling via
 * activate()/deactivate() and EntityService.despawnEntity().
 * </p>
 */
public class ProjectileComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(ProjectileComponent.class);
    /** Flag to prevent double-destruction */
    private boolean dead = false;

    /** Initial velocity (units/second) */
    // CHANGED: allow resetting for pooling
    private float vx, vy;

    /** Lifetime of the projectile in seconds */
    private float life;

    /** Remaining time before expiration */
    private float timer;

    /** Cached physics component for motion and collision */
    private PhysicsComponent physics;

    // NEW: pooling
    private boolean inactive = false;
    private String poolKey = null;

    // NEW: remember original scale to restore on reuse
    private float savedScaleX = 1f;
    private float savedScaleY = 1f;

    /**
     * @param vx   horizontal velocity (units/second)
     * @param vy   vertical velocity (units/second)
     * @param life lifetime in seconds
     */
    public ProjectileComponent(float vx, float vy, float life) {
        this.vx = vx;
        this.vy = vy;
        this.life = life;
    }

    /** Apply initial velocity and cache physics on first create. */
    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        timer = life;
        // Save initial scale for reuse
        try {
            var s = entity.getScale();
            if (s != null) { savedScaleX = s.x; savedScaleY = s.y; }
        } catch (Throwable ignored) {}

        if (physics != null && physics.getBody() != null) {
            float setVx = vx, setVy = vy;
            if (!isFinite(setVx) || !isFinite(setVy)) {
                logger.warn("ProjectileComponent.create(): invalid vx/vy {}, {}; clamping to 0.", vx, vy);
                setVx = 0f; setVy = 0f;
            }
            physics.getBody().setLinearVelocity(setVx, setVy);
        }
    }

    /**
     * Advance lifetime and despawn when it elapses. Skips updates while inactive (pooled).
     */
    @Override
    public void update() {
        // Skip all logic when pooled inactive
        if (inactive) return;

        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer -= dt;

        if (timer <= 0f) {
            if (dead) return;
            dead = true;
            // Lifetime expired -> stop and despawn
            deactivate();
            despawnInternal();
        }
    }

    /**
     * Reactivate this projectile with new velocity and lifetime.
     * Restores scale/visibility, re-enables physics, and triggers "projectile.activated"
     * so other components can reset per-shot state.
     * @param newVx   horizontal velocity in world units per second
     * @param newVy   vertical velocity in world units per second
     * @param newLife new lifetime in seconds
     */
    public void activate(float newVx, float newVy, float newLife) {
        this.vx = newVx;
        this.vy = newVy;
        this.life = newLife;
        this.timer = newLife;
        this.dead = false;
        this.inactive = false;

        // Restore visible scale so sprite reappears
        try { entity.setScale(savedScaleX, savedScaleY); } catch (Throwable ignored) {}

        if (physics == null) physics = entity.getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            physics.getBody().setActive(true);
            physics.getBody().setLinearVelocity(vx, vy);
        }

        // Notify listeners to reset per-shot flags (e.g., TouchAttack/DestroyOnHit)
        try { entity.getEvents().trigger("projectile.activated"); } catch (Throwable ignored) {}
    }

    /**
     * Deactivate this projectile for pooling. Halts physics, hides sprite, and moves it off-screen.
     * Call EntityService.despawnEntity(...) afterwards to return it to the pool.
     */
    public void deactivate() {
        if (inactive) return;
        inactive = true;

        if (physics == null) physics = entity.getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            physics.getBody().setLinearVelocity(0, 0);
            physics.getBody().setActive(false);
        }

        // Hide visually so no lingering sprite remains even if still registered
        try { entity.setScale(0f, 0f); } catch (Throwable ignored) {}
        try { entity.setPosition(-10000f, -10000f); } catch (Throwable ignored) {}
    }

    /**
     * Set the pool key used by EntityService to group reusable projectiles.
     * @param key pool identifier (e.g., "bullet:images/bullet.png")
     * @return this for chaining
     */
    public ProjectileComponent setPoolKey(String key) {
        this.poolKey = key;
        return this;
    }

    /**
     * @return the pool key used for despawn/obtain, or null if not pooled
     */
    public String getPoolKey() {
        return poolKey;
    }

    /**
     * @return true if this projectile is currently inactive (pooled), false if active
     */
    public boolean isInactive() { return inactive; }

    // Helper: common despawn path
    private void despawnInternal() {
        // Already stopped & hidden in deactivate()
        try {
            var es = ServiceLocator.getEntityService();
            if (es != null) {
                es.despawnEntity(entity); // return to pool if poolKey set, else dispose later
                return;
            }
        } catch (Exception ignored) {}
        try { entity.dispose(); } catch (Exception ignored) {}
    }

    private static boolean isFinite(float v) {
        return !Float.isNaN(v) && !Float.isInfinite(v);
    }
}
