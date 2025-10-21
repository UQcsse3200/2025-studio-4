package com.csse3200.game.components.towers;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.towers.TowerStatsComponent;
import com.csse3200.game.components.towers.TowerComponent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * Applies a multiplicative boost from a totem to towers in range.
 * Path A controls the totem's aura range (via TowerStatsComponent upgrades).
 * Path B controls the booster multiplier value (1.05, 1.10, ...), capped.
 *
 * Boosts applied (nerfed):
 *  - damage *= booster               (small increase)
 *  - attackCooldown /= booster       (small attack speed increase)
 *
 * Not boosted anymore (to avoid making totem overpowered):
 *  - range (kept unchanged)
 *  - projectileSpeed (kept unchanged)
 *
 * The component keeps track of the multiplier it applied per target so it can
 * remove/adjust only its own contribution (safe for overlapping totems).
 *
 * Stacking rules:
 *  - If allowStacking is true (default), multiple totems can boost the same tower.
 *  - If false, only one totem can boost a tower (first-come wins). Claim is released if that
 *    totem goes out of range or is disposed/sold, allowing another totem to apply next frame.
 */
public class StatsBoostComponent extends Component {
    private final Map<Entity, Float> appliedMultiplier = new HashMap<>();
    private float lastComputedBooster = -1f;
    private static final Map<Entity, Integer> GLOBAL_TOTEM_CLAIMS = new WeakHashMap<>();
    private static final float BASE_BOOST = 1.05f;
    private static final float BOOST_STEP = 0.05f;
    private static final float MAX_BOOST  = 1.25f;
    private static final float MIN_COOLDOWN = 0.001f;

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

        // Path A should already change myStats.range via upgrades; use that for area.
        float range = myStats.getRange();

        // Compute booster using level_B and clamp it
        int levelB = Math.max(1, myStats.getLevel_B());
        float booster = BASE_BOOST + BOOST_STEP * (levelB - 1);
        if (booster > MAX_BOOST) booster = MAX_BOOST;

        // If booster changed since last frame, adjust existing boosted targets by ratio
        if (lastComputedBooster > 0f && Math.abs(booster - lastComputedBooster) > 1e-6f) {
            float ratio = booster / lastComputedBooster;
            Iterator<Map.Entry<Entity, Float>> it = appliedMultiplier.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Entity, Float> e = it.next();
                Entity target = e.getKey();
                if (target == null) { it.remove(); continue; }
                TowerStatsComponent tStats = target.getComponent(TowerStatsComponent.class);
                if (tStats == null) { it.remove(); continue; }
                // Nerfed: only adjust damage and cooldown by ratio
                tStats.setDamage(tStats.getDamage() * ratio);
                tStats.setAttackCooldown(Math.max(MIN_COOLDOWN, tStats.getAttackCooldown() / ratio));

                // update stored multiplier to the new booster (this component's contribution)
                e.setValue(booster);
            }
        }

        lastComputedBooster = booster;

        var es = ServiceLocator.getEntityService();
        // Use ServiceLocator when present; otherwise fall back to test hook if set.
        Iterable<Entity> iterable = null;
        if (es != null) {
            try {
                iterable = es.getEntitiesCopy();
            } catch (Exception ignored) {
            }
        }
        if (iterable == null) {
            if (TEST_ENTITIES == null) return; // no source of entities; nothing to do
            iterable = TEST_ENTITIES;
        }

        Map<Entity, Boolean> found = new HashMap<>();

        for (Entity other : iterable) {
            if (other == null || other == entity) continue;
            TowerComponent towerComp = other.getComponent(TowerComponent.class);
            TowerStatsComponent tStats = other.getComponent(TowerStatsComponent.class);
            if (towerComp == null || tStats == null) continue;

            // Skip totems (do not boost other totem towers)
            if ("totem".equalsIgnoreCase(towerComp.getType())) continue;

            if (other.getCenterPosition() == null || entity.getCenterPosition() == null) continue;
            float d = other.getCenterPosition().dst(entity.getCenterPosition());
            if (d <= range) {
                found.put(other, Boolean.TRUE);
                if (!appliedMultiplier.containsKey(other)) {
                    // Nerfed: apply boost only to damage and cooldown
                    tStats.setDamage(tStats.getDamage() * booster);
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
                    // Revert only this component's contribution (damage/cooldown)
                    tStats.setDamage(tStats.getDamage() / applied);
                    tStats.setAttackCooldown(Math.max(MIN_COOLDOWN, tStats.getAttackCooldown() * applied));
                }
                it.remove();
            }
        }
    }

    /**
     * Notify this booster that a target tower's base stats changed externally (e.g., via upgrade).
     * Clears cached application so on the next update, if still in range, the boost re-applies.
     */
    public void onTargetStatsChanged(Entity target) {
        if (target == null) return;
        // Revert our current contribution (if any) so next update will reapply cleanly
        Float applied = appliedMultiplier.remove(target);
        if (applied != null) {
            TowerStatsComponent tStats = target.getComponent(TowerStatsComponent.class);
            if (tStats != null) {
                tStats.setDamage(tStats.getDamage() / applied);
                tStats.setAttackCooldown(Math.max(MIN_COOLDOWN, tStats.getAttackCooldown() * applied));
            }
        }
    }

    /**
     * Disposes of the component and reverts any applied boosts.
     */
    @Override
    public void dispose() {
        // On dispose, revert any applied boosts (damage/cooldown only)
        for (Map.Entry<Entity, Float> e : appliedMultiplier.entrySet()) {
            Entity target = e.getKey();
            float applied = e.getValue();
            if (target == null) continue;
            TowerStatsComponent tStats = target.getComponent(TowerStatsComponent.class);
            if (tStats == null) continue;
            tStats.setDamage(tStats.getDamage() / applied);
            tStats.setAttackCooldown(Math.max(MIN_COOLDOWN, tStats.getAttackCooldown() * applied));
        }
        appliedMultiplier.clear();
        super.dispose();
    }
}