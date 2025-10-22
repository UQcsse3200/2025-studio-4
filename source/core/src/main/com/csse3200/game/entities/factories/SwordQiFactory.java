package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.hero.samurai.SwordQiAnimatorPng;
import com.csse3200.game.components.projectile.DestroyOnHitComponent;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.entities.configs.DamageTypeConfig;

/**
 * Factory dedicated to creating the “Cyber Samurai Sword Qi” projectile (PNG sprite sheet animation).
 * Does NOT add any static sprite rendering component.
 */
public final class SwordQiFactory {
    private SwordQiFactory() {}

    /**
     * @param spawn         spawn position (world coordinates)
     * @param vx,vy         initial velocity
     * @param life          lifetime (seconds)
     * @param damage        damage amount
     * @param drawW,drawH   render size (world units, corresponds to entity.setScale)
     * @param pngPath       PNG sprite sheet path (relative to assets)
     * @param cols,rows     grid slicing (columns/rows)
     * @param frameW,frameH per-frame pixel size
     * @param frameDur      per-frame duration (seconds)
     * @param angleDeg      world facing (0=right, 90=up)
     * @param baseRotation  asset’s base facing (asset facing up=90f, facing right=0f)
     * @param loop          whether the animation loops
     */
    public static Entity createSwordQi(
            Vector2 spawn,
            float vx, float vy,
            float life,
            int damage,
            float drawW, float drawH,
            String pngPath,
            int cols, int rows, int frameW, int frameH,
            float frameDur,
            float angleDeg,
            float baseRotation,
            boolean loop
    ) {
        // ===== 0) Safety: ensure all key params are finite =====
        if (spawn == null
                || !Float.isFinite(spawn.x) || !Float.isFinite(spawn.y)
                || !Float.isFinite(vx)     || !Float.isFinite(vy)
                || !Float.isFinite(life)   || life <= 0f
                || !Float.isFinite(drawW)  || !Float.isFinite(drawH) || drawW <= 0f || drawH <= 0f
                || !Float.isFinite(angleDeg) || !Float.isFinite(baseRotation)
                || cols <= 0 || rows <= 0 || frameW <= 0 || frameH <= 0
                || !Float.isFinite(frameDur) || frameDur <= 0f) {
            // Abort creation to avoid JNI crashes
            com.badlogic.gdx.Gdx.app.error("SwordQiFactory", "Bad params; skip create. spawn="+spawn+" vx="+vx+" vy="+vy+" angle="+angleDeg);
            return null;
        }

        // Optional coordinate clamping (avoid extreme positions)
        float px = com.badlogic.gdx.math.MathUtils.clamp(spawn.x, -10000f, 10000f);
        float py = com.badlogic.gdx.math.MathUtils.clamp(spawn.y, -10000f, 10000f);
        float safeAngle = angleDeg;
        // Normalize angle to within [-360, 360] to avoid meaningless large angles
        while (safeAngle > 360f) safeAngle -= 360f;
        while (safeAngle < -360f) safeAngle += 360f;

        // ===== 1) Assemble components =====
        var physics = new com.csse3200.game.physics.components.PhysicsComponent();
        physics.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.KinematicBody);

        var hitbox = new com.csse3200.game.physics.components.HitboxComponent();
        hitbox.setLayer(com.csse3200.game.physics.PhysicsLayer.PROJECTILE);
        hitbox.setSensor(true); // Projectiles use sensors

        Entity qi = new Entity()
                .addComponent(physics)
                .addComponent(hitbox)
                .addComponent(new ProjectileComponent(vx, vy, life))
                .addComponent(new CombatStatsComponent(
                        1, damage, DamageTypeConfig.None, DamageTypeConfig.None))
                .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, 0f))
                .addComponent(new DestroyOnHitComponent(PhysicsLayer.NPC));

        // Set render size and position first (usually before entity.create())
        qi.setScale(drawW, drawH);
        qi.setPosition(px, py);

        // ===== 2) Safely add PNG animation renderer =====
        SwordQiAnimatorPng.apply(
                qi, pngPath, cols, rows, frameW, frameH, frameDur,
                safeAngle, baseRotation, loop
        );

        // ===== 3) Do NOT touch the body immediately here =====
        // Note: PhysicsComponent’s body may not be created yet (it’s created after the entity enters the world).
        // Calling getBody().setTransform(...) at the wrong time can NPE or crash JNI.
        //
        // Safer approach: defer rotation sync to the next frame (body will exist by then) and check for null again.
        final float fx = px;
        final float fy = py;
        final float fAngleRad = (float)Math.toRadians(safeAngle + baseRotation);
        final var physicsRef = physics; // Optional, just for clarity

        com.badlogic.gdx.Gdx.app.postRunnable(() -> {
            if (physicsRef.getBody() != null) {
                physicsRef.getBody().setTransform(fx, fy, fAngleRad);
            }
        });

        return qi;
    }

}
