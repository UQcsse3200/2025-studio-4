package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.components.projectile.DestroyOnHitComponent;
import com.csse3200.game.entities.configs.DamageTypeConfig;

/**
 * Factory class for creating projectile (bullet) entities.
 * <p>
 * Provides standard methods for constructing bullets with:
 * - Physics and hitbox for collision handling.
 * - Render component for visuals.
 * - {@link ProjectileComponent} for velocity and lifetime control.
 * - {@link CombatStatsComponent} to store bullet damage.
 * - {@link TouchAttackComponent} to apply damage to enemies.
 * - {@link DestroyOnHitComponent} to destroy the bullet on collision.
 * </p>
 */
public final class ProjectileFactory {
    private ProjectileFactory() {
        throw new IllegalStateException("Static factory class, do not instantiate");
    }

    /**
     * Create a bullet with default damage.
     *
     * @param texture  path to bullet texture
     * @param startPos initial spawn position
     * @param vx       horizontal velocity
     * @param vy       vertical velocity
     * @param life     lifetime in seconds
     * @return a new bullet entity
     */
    public static Entity createBullet(String texture, Vector2 startPos, float vx, float vy, float life) {
        int defaultDamage = 25;
        return createBullet(texture, startPos, vx, vy, life, defaultDamage);
    }

    /**
     * Create a bullet with custom damage.
     *
     * @param texture  path to bullet texture
     * @param startPos initial spawn position
     * @param vx       horizontal velocity
     * @param vy       vertical velocity
     * @param life     lifetime in seconds
     * @param damage   bullet damage
     * @return a new bullet entity
     */
    public static Entity createBullet(
            String texture, Vector2 startPos,
            float vx, float vy, float life, int damage
    ) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.KinematicBody);

        HitboxComponent hitbox = new HitboxComponent();
        hitbox.setLayer(PhysicsLayer.PROJECTILE);
        hitbox.setSensor(true);

        RotatingTextureRenderComponent render = new RotatingTextureRenderComponent(texture);

        Entity bullet = new Entity()
                .addComponent(physics)
                .addComponent(hitbox)
                .addComponent(render) // ✅
                .addComponent(new ProjectileComponent(vx, vy, life))
                .addComponent(new CombatStatsComponent(
                        1, damage,
                        DamageTypeConfig.None,
                        DamageTypeConfig.None
                ))
                .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, 0f))
                .addComponent(new DestroyOnHitComponent(PhysicsLayer.NPC));

        bullet.setPosition(startPos);


        float angleDeg = (float) Math.toDegrees(Math.atan2(vy, vx));
        float BULLET_FACING_OFFSET_DEG = -90f;
        render.setRotation(angleDeg + BULLET_FACING_OFFSET_DEG);

        return bullet;
    }

}





