package com.csse3200.game.components.hero.samurai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.HitboxComponent;

/**
 * 只有在 SwordJabPhysicsComponent.isAttacking()==true 时结算伤害。
 * 支持两种 CombatStats API：
 *  1) targetStats.hit(attackerStats)  // 常见于你们项目
 *  2) targetStats.hit(int damage)     // 若也存在此重载则可用
 */
public class AttackOnContactIfAttacking extends Component {
    private final int damage;                 // 仅在有 int 重载时才会用到
    private final short targetLayer;
    private final float perTargetCooldownSec;

    private HitboxComponent hitbox;
    private SwordJabPhysicsComponent jabCtrl;
    private float spawnMuteTimer = 0.05f; // 50ms 静默


    // 建议把“攻击者实体”作为构造参数传进来（武士/持有者）
    private final Entity attackerOwner; // 可为 null，则退化为用本 entity 作为攻击者

    private final ObjectFloatMap<Entity> targetCd = new ObjectFloatMap<>();

    public AttackOnContactIfAttacking(int damage, short targetLayer, float perTargetCooldownSec) {
        this(null, damage, targetLayer, perTargetCooldownSec);
    }

    public AttackOnContactIfAttacking(Entity attackerOwner,
                                      int damage,
                                      short targetLayer,
                                      float perTargetCooldownSec) {
        this.attackerOwner = attackerOwner;
        this.damage = damage;
        this.targetLayer = targetLayer;
        this.perTargetCooldownSec = Math.max(0f, perTargetCooldownSec);
    }

    @Override
    public void create() {
        hitbox = entity.getComponent(HitboxComponent.class);
        if (hitbox == null) {
            throw new IllegalStateException("AttackOnContactIfAttacking requires HitboxComponent on sword entity.");
        }
        jabCtrl = entity.getComponent(SwordJabPhysicsComponent.class);
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    }

    @Override
    public void update() {
        if (targetCd.size == 0) return;
        float dt = Gdx.graphics.getDeltaTime();
        var toRemove = new java.util.ArrayList<Entity>();
        for (ObjectFloatMap.Entry<Entity> e : targetCd) {
            float left = e.value - dt;
            if (left <= 0f) toRemove.add(e.key);
            else targetCd.put(e.key, left);
        }
        for (Entity e : toRemove) targetCd.remove(e, 0f);
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        // 0) 生成静默：避免生成瞬间重叠触发
        if (spawnMuteTimer > 0f) return;

        // 1) 只处理本命中体
        if (hitbox == null || hitbox.getFixture() != me) return;

        // 2) 层过滤
        short otherBits = other.getFilterData() != null ? other.getFilterData().categoryBits : 0;
        if (!com.csse3200.game.physics.PhysicsLayer.contains(targetLayer, otherBits)) return;

        // 3) 取对方实体
        Object ud = other.getUserData();
        if (!(ud instanceof Entity target)) return;
        if (target == this.entity) return; // 排除自碰撞

        // 4) 攻击窗口（严格）
        if (jabCtrl == null) return;
        if (!jabCtrl.isAttacking()) return;

        // 5) 单体去抖
        if (targetCd.containsKey(target)) return;

        // 6) 结算伤害（支持 hit(attackerStats) 或 hit(int)）
        CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
        if (targetStats == null) return;

        Entity atkEntity = (attackerOwner != null) ? attackerOwner : this.entity;
        CombatStatsComponent attackerStats = (atkEntity != null)
                ? atkEntity.getComponent(CombatStatsComponent.class) : null;

        com.badlogic.gdx.Gdx.app.postRunnable(() -> {
            boolean dealt = false;

            if (attackerStats != null) {
                try {
                    targetStats.getClass()
                            .getMethod("hit", CombatStatsComponent.class)
                            .invoke(targetStats, attackerStats);
                    dealt = true;
                } catch (ReflectiveOperationException ignored) {}
            }

            if (!dealt) {
                try {
                    targetStats.getClass()
                            .getMethod("hit", int.class)
                            .invoke(targetStats, damage);
                    dealt = true;
                } catch (ReflectiveOperationException ignored) {}
            }

            if (dealt) {
                targetCd.put(target, perTargetCooldownSec);
                // 可在此触发命中特效/音效
                // target.getEvents().trigger("weaponHit", this.entity);
            }
        });
    }

}

