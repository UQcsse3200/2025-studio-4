package core.src.main.com.csse3200.game.components.towers;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.HashSet;
import java.util.Set;

/**
 * Boosts stats of nearby towers by multiplying damage, range, projectile speed, and cooldown by 1.3.
 */
public class StatsBoostComponent extends Component {
    private final float boostMultiplier = 1.3f;
    private final Set<Entity> boosted = new HashSet<>();
    private float range;

    public StatsBoostComponent(float range) {
        this.range = range;
    }

    @Override
    public void update() {
        Vector2 myCenter = entity.getCenterPosition();
        Set<Entity> currentlyInRange = new HashSet<>();

        for (Entity other : ServiceLocator.getEntityService().getEntitiesCopy()) {
            if (other == entity) continue;
            TowerStatsComponent stats = other.getComponent(TowerStatsComponent.class);
            TowerComponent tower = other.getComponent(TowerComponent.class);
            if (stats == null || tower == null) continue;

            Vector2 otherCenter = other.getCenterPosition();
            if (otherCenter == null) continue;
            float dist = otherCenter.dst(myCenter);
            if (dist <= range) {
                currentlyInRange.add(other);
                if (!boosted.contains(other)) {
                    stats.setDamage(stats.getDamage() * boostMultiplier);
                    stats.setRange(stats.getRange() * boostMultiplier);
                    stats.setProjectileSpeed(stats.getProjectileSpeed() * boostMultiplier);
                    stats.setAttackCooldown(stats.getAttackCooldown() * boostMultiplier);
                    boosted.add(other);
                }
            }
        }

        // Remove boost from towers that left the range
        for (Entity e : new HashSet<>(boosted)) {
            if (!currentlyInRange.contains(e)) {
                TowerStatsComponent stats = e.getComponent(TowerStatsComponent.class);
                if (stats != null) {
                    stats.setDamage(stats.getDamage() / boostMultiplier);
                    stats.setRange(stats.getRange() / boostMultiplier);
                    stats.setProjectileSpeed(stats.getProjectileSpeed() / boostMultiplier);
                    stats.setAttackCooldown(stats.getAttackCooldown() / boostMultiplier);
                }
                boosted.remove(e);
            }
        }
    }
}
