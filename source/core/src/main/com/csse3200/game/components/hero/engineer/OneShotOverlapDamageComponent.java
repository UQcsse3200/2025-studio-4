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
 * Non-blocking one-shot contact damage: each target (by Entity) takes damage once on first entry,
 * and won't be hit again afterward.
 * Requires: this entity has a HitboxComponent (for overlap events); optional CombatStatsComponent (as the attacker).
 */
public class OneShotOverlapDamageComponent extends Component {
    private final short targetMask;          // e.g., PhysicsLayer.NPC
    private final int fallbackDamage;        // If this entity lacks CombatStats, use this damage
    private HitboxComponent hitbox;
    private CombatStatsComponent myStats;

    // Targets already hit to prevent repeated damage
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

        // Apply damage once (pass the attacker's CombatStatsComponent; if absent, use a temporary one)
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
