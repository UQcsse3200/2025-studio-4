package com.csse3200.game.components.towers;

import com.csse3200.game.components.Component;k
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.towers.TowerStatsComponent;
import com.csse3200.game.components.towers.TowerComponent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Applies a multiplicative boost from a totem to towers in range.
 * Path A controls the totem's range (already handled by TowerStatsComponent upgrades).
 * Path B controls the booster multiplier value (1.1, 1.2, ...).
 *
 * Boosts applied:
 *  - damage *= booster
 *  - range *= booster
 *  - projectileSpeed *= booster
 *  - attackCooldown /= booster   (faster attacks)
 *
 * The component keeps track of the multiplier it applied per-target so it can
 * remove/adjust only its own contribution (safe for overlapping totems).
 */
public class StatsBoostComponent extends Component {
    private final Map<Entity, Float> appliedMultiplier = new HashMap<>();
    private float lastComputedBooster = -1f;

    // Base booster when level_B == 1
    private static final float BASE_BOOST = 1.1f;
    private static final float BOOST_STEP = 0.1f;
    private static final float MIN_COOLDOWN = 0.001f;

    @Override
    public void create() {
        super.create();
        // nothing special on create
    }

    @Override
    public void update() {
        if (entity == null) return;
        TowerStatsComponent myStats = entity.getComponent(TowerStatsComponent.class);
        if (myStats == null) return;

        // Path A should already change myStats.range via upgrades; use that for area.
        float range = myStats.getRange();

        // Compute booster using level_B: 1.1 + 0.1*(level_B - 1)
        int levelB = Math.max(1, myStats.getLevel_B());
        float booster = BASE_BOOST + BOOST_STEP * (levelB - 1);

        // If booster changed since last frame, adjust existing boosted targets by ratio
        if (lastComputedBooster > 0f && Math.abs(booster - lastComputedBooster) > 1e-6f) {
            float ratio = booster / lastComputedBooster;
            // multiply damage/range/projSpeed by ratio, divide cooldown by ratio (faster)
            Iterator<Map.Entry<Entity, Float>> it = appliedMultiplier.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Entity, Float> e = it.next();
                Entity target = e.getKey();
                if (target == null) {
                    it.remove();
                    continue;
                }
                TowerStatsComponent tStats = target.getComponent(TowerStatsComponent.class);
                if (tStats == null) {
                    it.remove();
                    continue;
                }
                // Update stats by ratio
                tStats.setDamage(tStats.getDamage() * ratio);
                tStats.setRange(tStats.getRange() * ratio);
                tStats.setProjectileSpeed(tStats.getProjectileSpeed() * ratio);
                // cooldown: we previously divided by old booster; to increase speed again divide by ratio
                tStats.setAttackCooldown(Math.max(MIN_COOLDOWN, tStats.getAttackCooldown() / ratio));

                // update stored multiplier to the new booster (this component's contribution)
                e.setValue(booster);
            }
        }

        lastComputedBooster = booster;

        // Find towers in range and ensure they have the boost applied; remove for those leaving
        var es = ServiceLocator.getEntityService();
        if (es == null) return;

        // Build set of currently in-range towers (we'll mark found ones)
        Map<Entity, Boolean> found = new HashMap<>();

        for (Entity other : es.getEntitiesCopy()) {
            if (other == null || other == entity) continue;
            TowerComponent towerComp = other.getComponent(TowerComponent.class);
            TowerStatsComponent tStats = other.getComponent(TowerStatsComponent.class);
            if (towerComp == null || tStats == null) continue;

            // Skip totems (do not boost other totem towers)
            if ("totem".equalsIgnoreCase(towerComp.getType())) continue;

            // Check distance
            if (other.getCenterPosition() == null || entity.getCenterPosition() == null) continue;
            float d = other.getCenterPosition().dst(entity.getCenterPosition());
            if (d <= range) {
                found.put(other, Boolean.TRUE);
                if (!appliedMultiplier.containsKey(other)) {
                    // Apply this component's booster
                    tStats.setDamage(tStats.getDamage() * booster);
                    tStats.setRange(tStats.getRange() * booster);
                    tStats.setProjectileSpeed(tStats.getProjectileSpeed() * booster);
                    tStats.setAttackCooldown(Math.max(MIN_COOLDOWN, tStats.getAttackCooldown() / booster));
                    appliedMultiplier.put(other, booster);
                }
            }
        }

        // Remove boost from towers no longer in range
        Iterator<Map.Entry<Entity, Float>> it = appliedMultiplier.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, Float> e = it.next();
            Entity target = e.getKey();
            float applied = e.getValue();
            if (target == null || !found.containsKey(target)) {
                TowerStatsComponent tStats = (target != null) ? target.getComponent(TowerStatsComponent.class) : null;
                if (tStats != null) {
                    // Revert this component's contribution
                    // For damage/range/projSpeed divide by applied multiplier
                    tStats.setDamage(tStats.getDamage() / applied);
                    tStats.setRange(tStats.getRange() / applied);
                    tStats.setProjectileSpeed(tStats.getProjectileSpeed() / applied);
                    // For cooldown, multiply back
                    tStats.setAttackCooldown(Math.max(MIN_COOLDOWN, tStats.getAttackCooldown() * applied));
                }
                it.remove();
            }
        }
    }

    @Override
    public void dispose() {
        // On dispose, revert any applied boosts
        for (Map.Entry<Entity, Float> e : appliedMultiplier.entrySet()) {
            Entity target = e.getKey();
            float applied = e.getValue();
            if (target == null) continue;
            TowerStatsComponent tStats = target.getComponent(TowerStatsComponent.class);
            if (tStats == null) continue;
            tStats.setDamage(tStats.getDamage() / applied);
            tStats.setRange(tStats.getRange() / applied);
            tStats.setProjectileSpeed(tStats.getProjectileSpeed() / applied);
            tStats.setAttackCooldown(Math.max(MIN_COOLDOWN, tStats.getAttackCooldown() * applied));
        }
        appliedMultiplier.clear();
        super.dispose();
    }
}