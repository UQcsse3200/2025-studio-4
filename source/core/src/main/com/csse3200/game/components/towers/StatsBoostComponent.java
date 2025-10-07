package core.src.main.com.csse3200.game.components.towers;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.HashSet;
import java.util.Set;

/**
 * Boosts stats of nearby towers by multiplying their attributes.
 * Each stat has its own booster multiplier, which can be increased individually.
 */
public class StatsBoostComponent extends Component {
    // Individual stat boosters
    private float damageBooster = 1.3f;
    private float rangeBooster = 1.3f;
    private float cooldownBooster = 1.3f;
    private float projectileSpeedBooster = 1.3f;

    private final Set<Entity> boosted = new HashSet<>();
    private final float range;

    public StatsBoostComponent(float range) {
        this.range = range;
    }

    @Override
    public void update() {
        Vector2 myCenter = entity.getCenterPosition();
        if (myCenter == null) return;

        // --- Dynamically update boosters based on upgrade levels ---
        TowerStatsComponent stats = entity.getComponent(TowerStatsComponent.class);
        if (stats != null) {
            // Base values
            damageBooster = 1.3f;
            rangeBooster = 1.3f;
            cooldownBooster = 1.3f;
            projectileSpeedBooster = 1.3f;

            // For each level above 1, increase boosters
            int levelA = stats.getLevel_A();
            int levelB = stats.getLevel_B();

            // Each level above 1 increases by 0.1
            if (levelA > 1) {
                damageBooster += 0.1f * (levelA - 1);
                rangeBooster += 0.1f * (levelA - 1);
            }
            if (levelB > 1) {
                cooldownBooster += 0.1f * (levelB - 1);
                projectileSpeedBooster += 0.1f * (levelB - 1);
            }
        }
        // --- End booster update ---

        Set<Entity> currentlyInRange = new HashSet<>();

        for (Entity other : ServiceLocator.getEntityService().getEntitiesCopy()) {
            if (other == entity) continue;

            TowerStatsComponent otherStats = other.getComponent(TowerStatsComponent.class);
            TowerComponent tower = other.getComponent(TowerComponent.class);
            if (otherStats == null || tower == null) continue;

            Vector2 otherCenter = other.getCenterPosition();
            if (otherCenter == null) continue;

            float dist = otherCenter.dst(myCenter);
            if (dist <= range) {
                currentlyInRange.add(other);

                if (!boosted.contains(other)) {
                    applyBoost(otherStats);
                    boosted.add(other);
                }
            }
        }

        // Remove boost from towers that left the range
        for (Entity e : new HashSet<>(boosted)) {
            if (!currentlyInRange.contains(e)) {
                TowerStatsComponent statsToRemove = e.getComponent(TowerStatsComponent.class);
                if (statsToRemove != null) removeBoost(statsToRemove);
                boosted.remove(e);
            }
        }
    }

    /**
     * Applies the current stat boosts to a tower.
     */
    private void applyBoost(TowerStatsComponent stats) {
        stats.setDamage(stats.getDamage() * damageBooster);
        stats.setRange(stats.getRange() * rangeBooster);
        stats.setProjectileSpeed(stats.getProjectileSpeed() * projectileSpeedBooster);
        stats.setAttackCooldown(stats.getAttackCooldown() * cooldownBooster);
    }

    /**
     * Removes the current stat boosts from a tower.
     */
    private void removeBoost(TowerStatsComponent stats) {
        stats.setDamage(stats.getDamage() / damageBooster);
        stats.setRange(stats.getRange() / rangeBooster);
        stats.setProjectileSpeed(stats.getProjectileSpeed() / projectileSpeedBooster);
        stats.setAttackCooldown(stats.getAttackCooldown() / cooldownBooster);
    }

    // --- Booster Increment Functions ---

    public void increaseDamageBooster(float amount) {
        damageBooster += amount;
    }

    public void increaseRangeBooster(float amount) {
        rangeBooster += amount;
    }

    public void increaseCooldownBooster(float amount) {
        cooldownBooster += amount;
    }

    public void increaseProjectileSpeedBooster(float amount) {
        projectileSpeedBooster += amount;
    }

    // --- Optional Getters for Debugging or UI ---
    public float getDamageBooster() {
        return damageBooster;
    }

    public float getRangeBooster() {
        return rangeBooster;
    }

    public float getCooldownBooster() {
        return cooldownBooster;
    }

    public float getProjectileSpeedBooster() {
        return projectileSpeedBooster;
    }
}
