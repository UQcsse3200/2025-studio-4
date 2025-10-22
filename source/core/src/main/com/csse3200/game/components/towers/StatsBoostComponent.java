package com.csse3200.game.components.towers;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * Applies a permanent multiplicative boost from a totem to towers in range.
 * When a tower is within range, its base stats are effectively scaled via cumulative multipliers
 * in TowerStatsComponent (damage, range, projectileSpeed multiplied; attackCooldown divided).
 * If the totem's level changes, already-boosted towers are topped-up by the ratio.
 * When a boosted tower is upgraded (its base stats increase), the boosts are reapplied automatically
 * since current stats are derived from base stats and cumulative multipliers.
 */
public class StatsBoostComponent extends Component {
    // Track the booster value this totem has applied per target so we can top-up on totem level change
    private final Map<Entity, Float> appliedBoosterPerTarget = new HashMap<>();
    private float lastComputedBooster = -1f;

    // TEST HOOK: Optional list of entities used in tests when the EntityService is unavailable.
    private static List<Entity> TEST_ENTITIES = null;

    // Allow tests to inject entities list without ServiceLocator.
    static void setTestEntities(List<Entity> entities) {
        TEST_ENTITIES = entities;
    }

    /**
     * Initializes the component.
     */
    @Override
    public void create() {
        super.create();
        // nothing special on create
    }

    /**
     * Updates the boost logic, applies or removes boosts to towers in range.
     */
    @Override
    public void update() {
        if (entity == null) return;

        TowerStatsComponent myStats = entity.getComponent(TowerStatsComponent.class);
        if (myStats == null) return;

        float range = myStats.getRange();

        // Compute booster using level_B (1.05, 1.10, ...), clamp to a sensible cap
        int levelB = Math.max(1, myStats.getLevel_B());
        float booster = 1.05f + 0.05f * (levelB - 1);
        if (booster > 1.25f) booster = 1.25f;

        var es = ServiceLocator.getEntityService();
        // Use ServiceLocator when present; otherwise fall back to test hook if set.
        Iterable<Entity> iterable = null;
        if (es != null) {
            try {
                iterable = es.getEntitiesCopy();
            } catch (Exception ignored) {}
        }
        if (iterable == null) {
            if (TEST_ENTITIES == null) return;
            iterable = TEST_ENTITIES;
        }

        // Apply/top-up to towers currently within range
        for (Entity other : iterable) {
            if (other == null || other == entity) continue;

            TowerComponent towerComp = other.getComponent(TowerComponent.class);
            TowerStatsComponent tStats = other.getComponent(TowerStatsComponent.class);
            if (towerComp == null || tStats == null) continue;
            if ("totem".equalsIgnoreCase(towerComp.getType())) continue;

            if (other.getCenterPosition() == null || entity.getCenterPosition() == null) continue;
            float d = other.getCenterPosition().dst(entity.getCenterPosition());
            if (d > range) continue;

            Float applied = appliedBoosterPerTarget.get(other);
            if (applied == null) {
                // First time in range: apply multipliers
                tStats.applyPermanentBoostMultipliers(booster, booster, booster, booster);
                appliedBoosterPerTarget.put(other, booster);
            } else if (Math.abs(booster - applied) > 1e-6f) {
                // Totem level changed: top-up by ratio
                float ratio = booster / applied;
                tStats.applyPermanentBoostMultipliers(ratio, ratio, ratio, ratio);
                appliedBoosterPerTarget.put(other, booster);
            }
        }

        // Revert boosts for targets no longer in range or now invalid
        Iterator<Map.Entry<Entity, Float>> it = appliedBoosterPerTarget.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, Float> e = it.next();
            Entity target = e.getKey();
            float applied = e.getValue();

            if (target == null || !target.isActive()) {
                // Target gone; just drop tracking
                it.remove();
                continue;
            }
            if (target.getCenterPosition() == null || entity.getCenterPosition() == null) {
                // Unable to validate; revert to be safe
                TowerStatsComponent ts = target.getComponent(TowerStatsComponent.class);
                if (ts != null) ts.removePermanentBoostMultipliers(applied, applied, applied, applied);
                it.remove();
                continue;
            }

            float dist = target.getCenterPosition().dst(entity.getCenterPosition());
            if (dist > range) {
                // Out of range now: revert this totem's contribution
                TowerStatsComponent ts = target.getComponent(TowerStatsComponent.class);
                if (ts != null) ts.removePermanentBoostMultipliers(applied, applied, applied, applied);
                it.remove();
            }
        }

        lastComputedBooster = booster;
    }

    /**
     * Reapply boosts after a target tower's base stats changed (e.g., upgrade).
     * Current stats are derived from base stats and stored multipliers, so simply recompute.
     */
    public void onTargetStatsChanged(Entity target) {
        if (target == null) return;
        TowerStatsComponent tStats = target.getComponent(TowerStatsComponent.class);
        if (tStats == null) return;
        tStats.recomputeFromBaseMultipliers();
    }

    /**
     * Disposes of the component and reverts any applied boosts.
     */
    @Override
    public void dispose() {
        // When this totem is sold/destroyed, revert its effects on all tracked towers
        for (Map.Entry<Entity, Float> e : appliedBoosterPerTarget.entrySet()) {
            Entity target = e.getKey();
            float applied = e.getValue();
            if (target == null || !target.isActive()) continue;
            TowerStatsComponent ts = target.getComponent(TowerStatsComponent.class);
            if (ts != null) {
                ts.removePermanentBoostMultipliers(applied, applied, applied, applied);
            }
        }
        appliedBoosterPerTarget.clear();
        super.dispose();
    }
}