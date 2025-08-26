package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class TowerComponent extends Component {

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
                targetStats.hit(new CombatStatsComponent(0, (int) stats.getDamage()));
                stats.resetAttackTimer();
                break; // attack one target per update
            }
        }
    }
}
