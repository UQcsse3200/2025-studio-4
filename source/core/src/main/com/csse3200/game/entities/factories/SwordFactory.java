package com.csse3200.game.entities.factories;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.components.hero.SwordOrbitPhysicsComponent;

/**
 * Factory for creating the Samurai's sword child entity.
 * Responsibility: visuals + physics + orbit motion (no damage logic here).
 */
public final class SwordFactory {
    private SwordFactory() {}

    /**
     * Create a sword that orbits around the owner like a windmill blade.
     *
     * @param owner                 the samurai (used to compute orbit center)
     * @param swordTexture          path to sword texture
     * @param radius                orbit radius where the HANDLE sits (world units)
     * @param angularSpeedDeg       angular speed in degrees per second (CCW positive)
     * @param spriteForwardOffsetDeg sprite default facing: right=0, up=90, left=180, down=270
     * @param centerToHandle        distance from sprite center to the HANDLE along forward dir (usually negative)
     */
    public static Entity createSword(Entity owner,
                                     String swordTexture,
                                     float radius,
                                     float angularSpeedDeg,
                                     float spriteForwardOffsetDeg,
                                     float centerToHandle) {

        Entity sword = new Entity()
                // Order matters: Physics first, then collider/hitbox, then rendering, then orbit
                .addComponent(new PhysicsComponent())                        // Box2D body
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent()
                        .setLayer(PhysicsLayer.PLAYER_ATTACK)
                        .setSensor(true))
                .addComponent(new RotatingTextureRenderComponent(swordTexture))
                .addComponent(new SwordOrbitPhysicsComponent(owner, radius, angularSpeedDeg)
                        .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                        .setCenterToHandle(centerToHandle));

        return sword;
    }

    /** Overload with common defaults (forward=0°, centerToHandle=-0.25). */
    public static Entity createSword(Entity owner,
                                     String swordTexture,
                                     float radius,
                                     float angularSpeedDeg) {
        return createSword(owner, swordTexture, radius, angularSpeedDeg, 0f, -0.25f);
    }
}
