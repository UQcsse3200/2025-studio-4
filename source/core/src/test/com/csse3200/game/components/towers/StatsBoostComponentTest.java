package core.src.test.com.csse3200.game.components.towers;

import core.src.main.com.csse3200.game.components.towers.StatsBoostComponent;
import com.csse3200.game.components.towers.TowerStatsComponent;
import com.csse3200.game.components.towers.TowerComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StatsBoostComponentTest {

    @Test
    void testBoosterRemovedWhenOutOfRange() {
        Entity totem = new Entity();
        TowerStatsComponent totemStats = new TowerStatsComponent(
                100, 0f, 2f, 1f, 0f, 1f, "projectiles/bullet.png", 1, 1
        );
        totem.addComponent(totemStats);
        totem.addComponent(new TowerComponent("totem", 1, 1));
        StatsBoostComponent boost = new StatsBoostComponent();
        totem.addComponent(boost);

        Entity tower = new Entity();
        TowerStatsComponent towerStats = new TowerStatsComponent(
                100, 10f, 5f, 2f, 50f, 1f, "projectiles/bullet.png", 1, 1
        );
        tower.addComponent(towerStats);
        tower.addComponent(new TowerComponent("bone", 1, 1));

        // Place tower out of range
        totem.setPosition(0, 0);
        tower.setPosition(100, 100);

        boost.update();

        // Booster should NOT be applied
        assertEquals(10f, towerStats.getDamage(), 0.001f);
        assertEquals(5f, towerStats.getRange(), 0.001f);
        assertEquals(50f, towerStats.getProjectileSpeed(), 0.001f);
        assertEquals(2f, towerStats.getAttackCooldown(), 0.001f);
    }

}
