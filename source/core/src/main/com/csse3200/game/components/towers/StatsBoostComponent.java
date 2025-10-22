package com.csse3200.game.components.towers;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.math.Vector2;
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

    private Task rescanTask; // periodic rescan so newly placed towers get boosts quickly

    /**
     * Initializes the component.
     */
    @Override
    public void create() {
        super.create();
        // Only schedule periodic rescans during normal gameplay (not in unit tests)
        if (TEST_ENTITIES == null) {
            rescanTask = Timer.schedule(new Task() {
                @Override public void run() {
                    rescanAndApply();
                }
            }, 0f, 0.1f);
        }
    }

    /**
     * Updates the boost logic, applies or removes boosts to towers in range.
     */
    @Override
    public void update() {
        // Keep frame-based rescan as well (cheap + immediate on active frames)
        rescanAndApply();
    }

    // Extracted scan logic so both update() and Timer can reuse it
    private void rescanAndApply() {
        if (entity == null) return;

        TowerStatsComponent myStats = entity.getComponent(TowerStatsComponent.class);
        if (myStats == null) return;

        float range = myStats.getRange();

        int levelB = Math.max(1, myStats.getLevel_B());
        float booster = 1.05f + 0.05f * (levelB - 1);
        if (booster > 1.25f) booster = 1.25f;

        // Prefer test-provided entities during tests; otherwise use ServiceLocator
        Iterable<Entity> iterable = null;
        if (TEST_ENTITIES != null) {
            iterable = TEST_ENTITIES;
        } else {
            var es = ServiceLocator.getEntityService();
            if (es != null) {
                try {
                    iterable = es.getEntitiesCopy();
                } catch (Exception ignored) {}
            }
        }
        if (iterable == null) return;

        // Apply/top-up to towers currently within range
        for (Entity other : iterable) {
            if (other == null || other == entity) continue;

            TowerComponent towerComp = other.getComponent(TowerComponent.class);
            TowerStatsComponent tStats = other.getComponent(TowerStatsComponent.class);
            if (towerComp == null || tStats == null) continue;
            if ("totem".equalsIgnoreCase(towerComp.getType())) continue;

            // Fallback to getPosition() if center unavailable (helps in tests)
            Vector2 myPos = entity.getCenterPosition() != null ? entity.getCenterPosition() : entity.getPosition();
            Vector2 otherPos = other.getCenterPosition() != null ? other.getCenterPosition() : other.getPosition();
            if (myPos == null || otherPos == null) continue;

            float d = otherPos.dst(myPos);
            if (d > range) continue;

            Float applied = appliedBoosterPerTarget.get(other);
            if (applied == null) {
                tStats.applyPermanentBoostMultipliers(booster, booster, booster, booster);
                appliedBoosterPerTarget.put(other, booster);
            } else if (Math.abs(booster - applied) > 1e-6f) {
                float ratio = booster / applied;
                tStats.applyPermanentBoostMultipliers(ratio, ratio, ratio, ratio);
                appliedBoosterPerTarget.put(other, booster);
            }
        }

        // Revert boosts for targets no longer valid or out of range
        Iterator<Map.Entry<Entity, Float>> it = appliedBoosterPerTarget.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, Float> e = it.next();
            Entity target = e.getKey();
            float applied = e.getValue();

            // If target is gone or inactive, revert this totem's contribution and forget it
            if (target == null || !target.isActive()) {
                TowerStatsComponent ts = (target != null) ? target.getComponent(TowerStatsComponent.class) : null;
                if (ts != null) {
                    ts.removePermanentBoostMultipliers(applied, applied, applied, applied);
                }
                it.remove();
                continue;
            }

            Vector2 myPos = entity.getCenterPosition() != null ? entity.getCenterPosition() : entity.getPosition();
            Vector2 targetPos = target.getCenterPosition() != null ? target.getCenterPosition() : target.getPosition();
            if (myPos == null || targetPos == null) {
                TowerStatsComponent ts = target.getComponent(TowerStatsComponent.class);
                if (ts != null) ts.removePermanentBoostMultipliers(applied, applied, applied, applied);
                it.remove();
                continue;
            }

            float dist = targetPos.dst(myPos);
            if (dist > range) {
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
        // Stop periodic rescan
        if (rescanTask != null) {
            rescanTask.cancel();
            rescanTask = null;
        }
        // Revert this totem's effects on all tracked towers
        for (Map.Entry<Entity, Float> e : appliedBoosterPerTarget.entrySet()) {
            Entity target = e.getKey();
            float applied = e.getValue();
            if (target == null) continue;
            TowerStatsComponent ts = target.getComponent(TowerStatsComponent.class);
            if (ts != null) {
                ts.removePermanentBoostMultipliers(applied, applied, applied, applied);
            }
        }
        appliedBoosterPerTarget.clear();
        super.dispose();
    }
}

