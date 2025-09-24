package com.csse3200.game.components.hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Samurai spin attack:
 * - Spawns a sword child-entity and wires up components.
 * - Orbit motion is delegated to SwordOrbitPhysicsComponent (radial alignment).
 * - Deals damage once on collisionStart with ENEMY.
 */
public class SamuraiSpinAttackComponent extends Component {
    /** degrees per second (passed to SwordOrbitPhysicsComponent) */
    private final float angularSpeedDegPerSec;
    /** orbit radius where the HANDLE sits */
    private final float swordRadius;
    /** sword sprite path */
    private final String swordTexture;
    /** optional camera (kept for parity) */
    private final Camera camera;
    private SwordOrbitPhysicsComponent orbitRef;

    // tweakables forwarded to the orbit component
    private float spriteForwardOffsetDeg = 0f; // 贴图默认朝右=0，朝上=90
    private float centerToHandle = -1.0f;     // 贴图中心到剑柄距离（世界单位）

    private Entity sword;

    public SamuraiSpinAttackComponent(float angularSpeedDegPerSec,
                                      float swordRadius,
                                      String swordTexture,
                                      Camera camera) {
        this.angularSpeedDegPerSec = angularSpeedDegPerSec;
        this.swordRadius = swordRadius;
        this.swordTexture = swordTexture;
        this.camera = camera;
    }

    /** Optional fine-tuning, forwarded to SwordOrbitPhysicsComponent. */
    public SamuraiSpinAttackComponent setSpriteForwardOffsetDeg(float deg) {
        this.spriteForwardOffsetDeg = deg;
        return this;
    }
    public SamuraiSpinAttackComponent setCenterToHandle(float d) {
        this.centerToHandle = d;
        return this;
    }

    @Override
    public void create() {
        // 1) 用工厂创建剑（只含 物理/渲染/轨道）
        sword = com.csse3200.game.entities.factories.SwordFactory.createSword(
                this.entity,          // owner = samurai
                swordTexture,
                swordRadius,
                angularSpeedDegPerSec,
                spriteForwardOffsetDeg,  // 可调：贴图默认朝向
                centerToHandle           // 可调：中心到柄的偏移
        );

        // 2) 在这里统一挂“只在 collisionStart 结算一次伤害”的组件
        sword.addComponent(new MeleeDamageOnCollisionStart(
                com.csse3200.game.physics.PhysicsLayer.ENEMY,  // 目标层（等于你们的 NPC）
                this.entity                                   // 攻击者（武士本体）
        ));

        // 3) 注册
        var es = com.csse3200.game.services.ServiceLocator.getEntityService();
        if (es != null) {
            com.badlogic.gdx.Gdx.app.postRunnable(() -> es.register(sword));
        }

        // 4) （可选）全局倍率：用 setter 改轨道速度（不要 removeComponent）
        entity.getEvents().addListener("attack.multiplier", (Float multiplier) -> {
            float m = (multiplier != null && multiplier > 0f) ? multiplier : 1f;
            var orbit = sword.getComponent(com.csse3200.game.components.hero.SwordOrbitPhysicsComponent.class);
            if (orbit != null) {
                orbit.setAngularSpeedDeg(angularSpeedDegPerSec * m);
            }
        });
    }


    @Override
    public void dispose() {
        super.dispose();
        if (sword != null) {
            sword.dispose();
            sword = null;
        }
    }

    /**
     * File-local melee damage component:
     * Calls targetStats.hit(attackerStats) at collisionStart.
     * Replace with your shared component if you already have one.
     */
    private static final class MeleeDamageOnCollisionStart extends Component {
        private final short targetLayer;
        private final Entity damageOwner;
        private HitboxComponent hitbox;

        MeleeDamageOnCollisionStart(short targetLayer, Entity damageOwner) {
            this.targetLayer = targetLayer;
            this.damageOwner = damageOwner;
        }

        @Override
        public void create() {
            hitbox = entity.getComponent(HitboxComponent.class);
            entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        }

        private void onCollisionStart(Fixture me, Fixture other) {
            if (hitbox == null || hitbox.getFixture() != me) return;
            if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) return;

            Object ud = other.getBody().getUserData();
            if (!(ud instanceof Entity target)) return;

            CombatStatsComponent attackerStats =
                    (damageOwner != null) ? damageOwner.getComponent(CombatStatsComponent.class) : null;
            CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);

            if (attackerStats != null && targetStats != null) {
                targetStats.hit(attackerStats);
                target.getEvents().trigger("hit");
            }
        }
    }
}
