// java
package com.csse3200.game.components.towers;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;

import java.util.HashSet;
import java.util.Set;

/**
 * Applies projectile damage to each new enemy collision without destroying the projectile.
 * Unlimited distinct targets by default (per shot). Prevents duplicate damage while overlapping
 * the same target. Resets per shot on "projectile.activated" for pooling.
 */
public class PiercingDamageComponent extends Component {
    private final short targetLayer;

    private HitboxComponent myHitbox;
    private CombatStatsComponent myStats;

    // Track which entities have already been hit during this shot
    private final Set<Entity> hitOnce = new HashSet<>();

    public PiercingDamageComponent(short targetLayer) {
        this.targetLayer = targetLayer;
    }

    @Override
    public void create() {
        myHitbox = entity.getComponent(HitboxComponent.class);
        myStats = entity.getComponent(CombatStatsComponent.class);

        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("projectile.activated", this::resetPerShot);
    }

    private void resetPerShot() {
        hitOnce.clear();
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        if (myHitbox == null || myHitbox.getFixture() != me) return;
        if (other.getFilterData() == null) return;
        if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) return;

        Entity target = getEntityFromFixture(other);
        if (target == null || hitOnce.contains(target)) return;

        CombatStatsComponent victimStats = target.getComponent(CombatStatsComponent.class);
        if (victimStats == null || myStats == null) return;

        // Apply damage
        victimStats.hit(myStats);

        // Optional: show damage popup if supported
        try {
            int dmg = (int) myStats.getBaseAttack();
            target.getEvents().trigger("showDamage", dmg, target.getCenterPosition().cpy());
        } catch (Throwable ignored) {}

        hitOnce.add(target);
    }

    private static Entity getEntityFromFixture(Fixture fixture) {
        // Preferred: fixture userData is a HitboxComponent
        Object fud = fixture.getUserData();
        if (fud instanceof HitboxComponent) {
            return ((HitboxComponent) fud).getEntity();
        }
        // Fallback: body userData is BodyUserData
        Object bud = fixture.getBody().getUserData();
        if (bud instanceof BodyUserData) {
            return ((BodyUserData) bud).entity;
        }
        return null;
    }
}
