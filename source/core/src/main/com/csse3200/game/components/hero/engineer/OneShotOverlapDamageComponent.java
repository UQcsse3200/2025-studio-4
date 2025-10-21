package com.csse3200.game.components.hero.engineer;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.PlayerCombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;

import java.util.HashSet;
import java.util.Set;

/**
 * 非阻挡的一次性接触伤害：每个目标（按 Entity）首次进入时结算一次伤害，之后不再命中。
 * 需要：本体具备 HitboxComponent(用于 overlap 事件)，可选 CombatStatsComponent（作为攻击者）。
 */
public class OneShotOverlapDamageComponent extends Component {
    private final short targetMask;          // 例如 PhysicsLayer.NPC
    private final int fallbackDamage;        // 若本体没有 CombatStats，用这个伤害值
    private HitboxComponent hitbox;
    private CombatStatsComponent myStats;

    // 已命中的目标，防止重复结算
    private final Set<Entity> alreadyHit = new HashSet<>();

    public OneShotOverlapDamageComponent(short targetMask, int fallbackDamage) {
        this.targetMask = targetMask;
        this.fallbackDamage = Math.max(1, fallbackDamage);
    }

    @Override
    public void create() {
        hitbox = entity.getComponent(HitboxComponent.class);
        myStats = entity.getComponent(CombatStatsComponent.class);
        entity.getEvents().addListener("collisionStart", this::onStart);
    }

    private void onStart(Fixture me, Fixture other) {
        if (hitbox == null || hitbox.getFixture() != me) return;
        if (!PhysicsLayer.contains(targetMask, other.getFilterData().categoryBits)) return;

        Entity target = ((BodyUserData) other.getBody().getUserData()).entity;
        if (target == null || alreadyHit.contains(target)) return;

        // 结算一次伤害（按签名传“攻击者”的 CombatStatsComponent；没有就用临时的）
        CombatStatsComponent attacker = (myStats != null)
                ? myStats
                : new CombatStatsComponent(1, fallbackDamage, DamageTypeConfig.None, DamageTypeConfig.None);

        CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
        if (targetStats != null) {
            targetStats.hit(attacker);
            target.getEvents().trigger("showDamage",
                    (myStats != null ? Math.max(1, myStats.getBaseAttack()) : fallbackDamage),
                    target.getCenterPosition().cpy());
            alreadyHit.add(target);
            return;
        }

        PlayerCombatStatsComponent pcs = target.getComponent(PlayerCombatStatsComponent.class);
        if (pcs != null) {
            pcs.hit(attacker);
            alreadyHit.add(target);
        }
    }
}
