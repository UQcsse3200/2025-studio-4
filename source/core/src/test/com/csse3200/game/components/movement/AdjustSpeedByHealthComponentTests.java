package com.csse3200.game.components.movement;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.GruntEnemyFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.Difficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(GameExtension.class)
class AdjustSpeedByHealthComponentTest {
    private Entity grunt;
    private CombatStatsComponent stats;
    private WaypointComponent waypointComponent;
    private float initialHealth;

    @BeforeEach
    void setUp() {
        // Register core services
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());

        // Time service required by the ChaseTask
        ServiceLocator.registerTimeSource(new GameTime());
        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerResourceService(resourceService);
        resourceService.loadTextureAtlases(new String[]{"images/grunt_basic_spritesheet.atlas"});
        resourceService.loadAll();

        // Create player and waypoints
        Entity player = PlayerFactory.createPlayer();
        List<Entity> waypoints = new ArrayList<>();
        Entity waypoint = new Entity();
        waypoint.setPosition(0f, 0f);
        waypoints.add(waypoint);

        // Create grunt
        grunt = GruntEnemyFactory.createGruntEnemy(waypoints, player, Difficulty.EASY);
        grunt.create();

        // Get components and initial health value after grunt creation
        stats = grunt.getComponent(CombatStatsComponent.class);
        waypointComponent = grunt.getComponent(WaypointComponent.class);
        initialHealth = stats.getHealth();
        
        // Initialize the component by calling update once
        grunt.update();
    }

    @Test
    void testSpeedSameWhenHealthAboveFirstThreshold() {
        // Set health to 90% of the original value
        stats.setHealth((int) (initialHealth * 0.9f));

        float expectedSpeed = 0.8f;
        assertEquals(expectedSpeed, waypointComponent.getSpeed().x, 0.0001);
        assertEquals(expectedSpeed, waypointComponent.getSpeed().y, 0.0001);
    }

    @Test
    void testSpeedIncreasesWhenHealthEqualsFirstThreshold() {
        // Set health to 50% of the original value
        stats.setHealth((int) (initialHealth * 0.50f));
        grunt.update();

        float expectedSpeed = 1.4f;
        assertEquals(expectedSpeed, waypointComponent.getSpeed().x, 0.0001);
        assertEquals(expectedSpeed, waypointComponent.getSpeed().y, 0.0001);
    }

    @Test
    void testSpeedIncreasesWhenHealthBelowFirstThreshold() {
        // Set health to 40% of the initial value
        stats.setHealth((int) (initialHealth * 0.40f));
        grunt.update();

        float expectedSpeed = 1.4f;
        assertEquals(expectedSpeed, waypointComponent.getSpeed().x, 0.0001);
        assertEquals(expectedSpeed, waypointComponent.getSpeed().y, 0.0001);
    }

    @Test
    void testSpeedIncreasesWhenHealthEqualsSecondThreshold() {
        // Set health to 25% of the initial value
        stats.setHealth((int) (initialHealth * 0.25f));
        grunt.update();

        float expectedSpeed = 2.0f;
        assertEquals(expectedSpeed, waypointComponent.getSpeed().x, 0.0001);
        assertEquals(expectedSpeed, waypointComponent.getSpeed().y, 0.0001);
    }

    @Test
    void testSpeedIncreasesWhenHealthBelowSecondThreshold() {
        // Set health to 10% of the initial value
        stats.setHealth((int) (initialHealth * 0.10f));
        grunt.update();

        float expectedSpeed = 2.0f;
        assertEquals(expectedSpeed, waypointComponent.getSpeed().x, 0.0001);
        assertEquals(expectedSpeed, waypointComponent.getSpeed().y, 0.0001);
    }
}