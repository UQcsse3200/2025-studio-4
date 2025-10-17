package com.csse3200.game.components.towers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.projectile.ProjectileComponent;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.DroneEnemyFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.Component;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.Set;
import java.util.Collections;

/**
 * Component for frozen mammoth skull tower that freezes enemies inside range for 2 seconds.
 * Stores each affected enemy's previous speed and restores it when the timer expires or on dispose.
 */
public class EnemyFreezeComponent extends Component {
    private static final float FREEZE_DURATION = 1.0f;

    // Save original speeds and remaining freeze time per-entity. WeakHashMap prevents leaks.
    private final Map<Entity, Vector2> originalSpeeds = new WeakHashMap<>();
    private final Map<Entity, Float> freezeTimers = new WeakHashMap<>();

    // Track enemies that have already been frozen while they remain inside this tower's range.
    // Use a weak-key set to avoid preventing GC of entities.
    private final Set<Entity> hasFrozenWhileInRange = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public void update() {
        if (!entity.isActive()) return;

        TowerStatsComponent stats = entity.getComponent(TowerStatsComponent.class);
        if (stats == null) return;

        float dt = 0f;
        if (ServiceLocator.getTimeSource() != null) {
            dt = ServiceLocator.getTimeSource().getDeltaTime();
        }

        float range = stats.getRange();
        Vector2 myCenter = entity.getCenterPosition();

        // Iterate all entities and freeze/restore as needed
        for (Entity other : ServiceLocator.getEntityService().getEntitiesCopy()) {
            if (other == null || other == entity) continue;

            // Skip non-enemy targets (copy logic similar to TowerComponent.isEnemyTarget)
            if (other.getComponent(ProjectileComponent.class) != null) continue;
            CombatStatsComponent cs = other.getComponent(CombatStatsComponent.class);
            if (cs == null) continue;
            HitboxComponent hb = other.getComponent(HitboxComponent.class);
            if (hb == null || hb.getFixture() == null || hb.getFixture().getFilterData() == null) continue;
            short cat = hb.getFixture().getFilterData().categoryBits;
            if (!PhysicsLayer.contains(PhysicsLayer.NPC, cat)) continue;

            Vector2 otherCenter = other.getCenterPosition();
            if (otherCenter == null) continue;

            float d = otherCenter.dst(myCenter);
            boolean inRange = d <= range;

            WaypointComponent wc = other.getComponent(WaypointComponent.class);

            // If currently frozen (timer active), decrement timer and restore when expired.
            if (freezeTimers.containsKey(other)) {
                float remaining = freezeTimers.get(other) - dt;
                if (remaining <= 0f) {
                    Vector2 orig = originalSpeeds.remove(other);
                    freezeTimers.remove(other);
                    if (other != null && orig != null) {
                        DroneEnemyFactory.updateSpeed(other, orig);
                    }
                    // Keep hasFrozenWhileInRange entry so we don't re-freeze while they remain in range.
                } else {
                    freezeTimers.put(other, remaining);
                }
                // Do not re-trigger freezing while timer active.
                continue;
            }

            // Not frozen now.
            if (inRange) {
                // If this enemy has already been frozen during this stay in range, do nothing.
                if (hasFrozenWhileInRange.contains(other)) {
                    continue;
                }

                // If we don't have a waypoint component, can't control speed.
                if (wc == null) continue;

                // First time entering/rising into range since last leave: freeze once.
                Vector2 orig = wc.getSpeed().cpy();
                originalSpeeds.put(other, orig);
                // Use tower's projectileLife as freeze duration if provided (upgradeable), otherwise fallback
                float freezeDuration = stats.getProjectileLife() > 0f ? stats.getProjectileLife() : FREEZE_DURATION;
                freezeTimers.put(other, freezeDuration);

                hasFrozenWhileInRange.add(other);
                DroneEnemyFactory.updateSpeed(other, new Vector2(0f, 0f));
            } else {
                // Enemy is outside range. If it was marked as frozen-while-in-range, clear that mark so future re-entry can freeze again.
                if (hasFrozenWhileInRange.contains(other)) {
                    hasFrozenWhileInRange.remove(other);
                }
                // If an entity somehow left while frozen (shouldn't be frozenTimers.containsKey now because handled above),
                // ensure any lingering data is cleaned up.
                if (freezeTimers.containsKey(other)) {
                    Vector2 orig = originalSpeeds.remove(other);
                    freezeTimers.remove(other);
                    if (other != null && orig != null) {
                        DroneEnemyFactory.updateSpeed(other, orig);
                    }
                }
            }
        }

        // Clean up entries for entities that are no longer registered (dead/removed)
        Array<Entity> currentEntities = ServiceLocator.getEntityService().getEntitiesCopy();
        originalSpeeds.keySet().removeIf(e -> e == null || !currentEntities.contains(e, true));
        freezeTimers.keySet().removeIf(e -> e == null || !currentEntities.contains(e, true));
        hasFrozenWhileInRange.removeIf(e -> e == null || !currentEntities.contains(e, true));
    }

    @Override
    public void dispose() {
        // On dispose, restore any frozen enemies back to original speed
        for (Map.Entry<Entity, Vector2> entry : originalSpeeds.entrySet()) {
            Entity e = entry.getKey();
            Vector2 orig = entry.getValue();
            if (e != null && orig != null) {
                DroneEnemyFactory.updateSpeed(e, orig);
            }
        }
        originalSpeeds.clear();
        freezeTimers.clear();
        hasFrozenWhileInRange.clear();
        super.dispose();
    }
}
