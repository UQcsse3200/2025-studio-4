package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.hero.samurai.SwordJabPhysicsComponent;
import com.csse3200.game.components.hero.samurai.SwordJabPhysicsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;


/**
 * Factory for creating the Samurai's sword child entity.
 * Responsibility: visuals + physics + orbit motion (no damage logic here).
 */
public final class SwordFactory {
    private SwordFactory() {
    }

    /**
     * Create a sword that orbits around the owner like a windmill blade.
     *
     * @param owner                  the samurai (used to compute orbit center)
     * @param swordTexture           path to sword texture
     * @param radius                 orbit radius where the HANDLE sits (world units)
     * @param angularSpeedDeg        angular speed in degrees per second (CCW positive)
     * @param spriteForwardOffsetDeg sprite default facing: right=0, up=90, left=180, down=270
     * @param centerToHandle         distance from sprite center to the HANDLE along forward dir (usually negative)
     */
    public static Entity createSword(Entity owner,
                                     String swordTexture,
                                     float radius,
                                     float angularSpeedDeg,
                                     float spriteForwardOffsetDeg,
                                     float centerToHandle,
                                     int damage,           // 新增：伤害值
                                     float hitCooldown) {  // 新增：同一目标间隔（秒），如0.15f

        Entity sword = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent()
                        .setLayer(PhysicsLayer.PLAYER_ATTACK)
                        .setSensor(true))
                .addComponent(new RotatingTextureRenderComponent(swordTexture))
                .addComponent(new SwordJabPhysicsComponent(owner, /*restRadius=*/radius)
                        .setSpriteForwardOffsetDeg(spriteForwardOffsetDeg)
                        .setCenterToHandle(centerToHandle)
                        .setJabParams(0.18f, 0.8f)       // 时间/距离
                        .setJabCooldown(0.05f))
                // ✅ 关键：给剑一个“伤害是多少”的组件
                .addComponent(new CombatStatsComponent(
                        50, damage,
                        DamageTypeConfig.None,
                        DamageTypeConfig.None))
                // ✅ 关键：把“碰撞”转成“对 NPC 造成伤害”
                .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, hitCooldown));

        return sword;
    }


    /**
     * Overload with common defaults (forward=0°, centerToHandle=-0.25).
     */
    /** Overload with defaults: forward=0°, centerToHandle=-0.25, damage=20, hitCooldown=0.2s */
    public static Entity createSword(Entity owner,
                                     String swordTexture,
                                     float radius,
                                     float angularSpeedDeg) {
        return createSword(
                owner,
                swordTexture,
                radius,
                angularSpeedDeg,
                0f,       // spriteForwardOffsetDeg
                -0.25f,   // centerToHandle
                50,       // 默认伤害（按需改）
                0.2f      // 默认对同一目标的命中间隔（秒）
        );
    }

}
