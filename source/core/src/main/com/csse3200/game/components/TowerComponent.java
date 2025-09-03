package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.configs.DamageTypeConfig;

/**
 * Component representing a tower entity, including its type and size.
 * Handles tower attack logic in the update method.
 */
public class TowerComponent extends Component {
    private final String type;
    private final int width;  // in tiles
    private final int height; // in tiles

    /**
     * Constructs a single-tile tower component.
     * @param type The type of the tower
     */
    public TowerComponent(String type) {
        this(type, 1, 1); // default is 1x1
    }

    /**
     * Constructs a multi-tile tower component.
     * @param type The type of the tower
     * @param width Width in tiles
     * @param height Height in tiles
     */
    public TowerComponent(String type, int width, int height) {
        this.type = type;
        this.width = width;
        this.height = height;
    }

    /** @return The type of the tower */
    public String getType() {
        return type;
    }

    /** @return Width of the tower in tiles */
    public int getWidth() { return width; }

    /** @return Height of the tower in tiles */
    public int getHeight() { return height; }

    /**
     * Updates the tower logic, including attack timer and attacking entities in range.
     */
    @Override
    public void update() {
        TowerStatsComponent stats = entity.getComponent(TowerStatsComponent.class);
        if (stats == null) return;

        float delta = 1/60f; // or pass in the real delta time
        stats.updateAttackTimer(delta);

        if (!stats.canAttack()) return;

        // Iterate over all entities
        for (Entity other : ServiceLocator.getEntityService().getEntitiesCopy()) { // <-- use getEntitiesCopy()
            if (other == entity) continue;

            // Only attack entities that have CombatStatsComponent
            CombatStatsComponent targetStats = other.getComponent(CombatStatsComponent.class);
            if (targetStats == null) continue;

            Vector2 direction = other.getCenterPosition().cpy().sub(entity.getCenterPosition());
            if (direction.len() <= stats.getRange()) {
                targetStats.hit(
                    new CombatStatsComponent(0, (int) stats.getDamage(), DamageTypeConfig.None, DamageTypeConfig.None)
                );
                stats.resetAttackTimer();
                break; // attack one target per update
            }
        }
    }
}
