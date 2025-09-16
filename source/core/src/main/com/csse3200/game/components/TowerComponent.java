package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.physics.PhysicsLayer;

/**
 * Component representing a tower entity, including its type and size.
 * Handles tower attack logic in the update method.
 */
public class TowerComponent extends Component {
    private final String type;
    private final int width;  // in tiles
    private final int height; // in tiles
    private long lastFireNs = 0L; // last time a projectile was fired


    /**
     * Constructs a single-tile tower component.
     * @param type The type of the tower
     */
    public TowerComponent(String type) {
        this(type, 1, 1); // default is 1x1
    }

    /**
     * Constructs a multi-tile tower component.
     * @param type The type of the tower
     * @param width Width in tiles
     * @param height Height in tiles
     */
    public TowerComponent(String type, int width, int height) {
        this.type = type;
        this.width = width;
        this.height = height;
    }

    /** @return The type of the tower */
    public String getType() {
        return type;
    }

    /** @return Width of the tower in tiles */
    public int getWidth() { return width; }

    /** @return Height of the tower in tiles */
    public int getHeight() { return height; }

    /**checks if the enity is a valid enemy target for the tower
     * @param e the entity to check
     * @return true if the entity is a valid enemy target, false otherwise
     */
    private static boolean isEnemyTarget(Entity e) {
        // Ignore projectiles outright
        if (e.getComponent(ProjectileComponent.class) != null) return false;

        // Must be damageable
        if (e.getComponent(CombatStatsComponent.class) == null) return false;

        // Prefer physics-layer check: require NPC (or your enemy layer)
        HitboxComponent hb = e.getComponent(HitboxComponent.class);
        if (hb == null || hb.getFixture() == null || hb.getFixture().getFilterData() == null) return false;

        short cat = hb.getFixture().getFilterData().categoryBits;
        // If your enemies are on PhysicsLayer.NPC, keep NPC here; otherwise change to your ENEMY layer.
        return PhysicsLayer.contains(PhysicsLayer.NPC, cat);
    }

    /**
     * Updates the tower logic, including attack timer and attacking entities in range.
     */
    @Override
    public void update() {
        TowerStatsComponent stats = entity.getComponent(TowerStatsComponent.class);
        if (stats == null) {
            return;
        }
        // Use real frame delta if available
        float dt = com.badlogic.gdx.Gdx.graphics != null
                ? com.badlogic.gdx.Gdx.graphics.getDeltaTime()
                : (1f / 60f);

        stats.updateAttackTimer(dt);
        if (!stats.canAttack()) return;

        // Find a target in range (first found; change to nearest if you like)
        Entity target = null;
        Vector2 myCenter = entity.getCenterPosition();
        float range = stats.getRange();

        for (Entity other : ServiceLocator.getEntityService().getEntitiesCopy()) {
            //ensure targeting enemy
            if (other == entity) continue;
            if (!isEnemyTarget(other)) continue;

            // Only consider things that can be damaged
            CombatStatsComponent targetStats = other.getComponent(CombatStatsComponent.class);
            if (targetStats == null) continue;

            // In range?
            Vector2 toOther = other.getCenterPosition().cpy().sub(myCenter);
            if (toOther.len() <= range) {
                target = other;
                break; // lock first valid target; swap to "nearest" if needed
            }
        }

        if (target == null) return;

        // Compute normalized direction toward target
        Vector2 dir = target.getCenterPosition().cpy().sub(myCenter);
        if (dir.isZero(0.0001f)) return;
        dir.nor();

// Pull these from your TowerStatsComponent if you have getters:
        float speed = stats.getProjectileSpeed() != 0f ? stats.getProjectileSpeed() : 400f;
        float life  = stats.getProjectileLife()  != 0f ? stats.getProjectileLife()  : 2f;
        String tex  = stats.getProjectileTexture() != null ? stats.getProjectileTexture() : "images/bullet.png";
        int damage  = (int) stats.getDamage();

        float vx = dir.x * speed;
        float vy = dir.y * speed;

        //prevents double firing projectile
        long now = com.badlogic.gdx.utils.TimeUtils.nanoTime();
        long minGap = (long)(stats.getAttackCooldown() * 1_000_000_000L);
        if (now - lastFireNs < minGap) return;
        lastFireNs = now;

// Use your ProjectileFactory (it already adds TouchAttack + DestroyOnHit on NPC)
        Entity bullet = com.csse3200.game.entities.factories.ProjectileFactory.createBullet(
                tex, myCenter, vx, vy, life, damage
        );

// Register safely after the loop
        var es = ServiceLocator.getEntityService();
        if (es != null) {
            com.badlogic.gdx.Gdx.app.postRunnable(() -> es.register(bullet));
        }

        stats.resetAttackTimer();

    }

}
