package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.enemy.SpeedWaypointComponent;
import com.csse3200.game.components.enemy.WaypointComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.Difficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(GameExtension.class)
class EnemySpeedWaypointTest {

    @BeforeEach
    void setUp() {
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());

        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerResourceService(resourceService);
        resourceService.loadTextures(new String[]{
                "images/basement.png",
                "images/grunt_enemy.png",
                "images/boss_enemy.png",
                "images/drone_enemy.png",
                "images/tank_enemy.png"
        });
        resourceService.loadAll();

        DroneEnemyFactory.resetToDefaults();
        GruntEnemyFactory.resetToDefaults();
        TankEnemyFactory.resetToDefaults();
        BossEnemyFactory.resetToDefaults();
    }

    @Test
    void droneUsesWaypointSpeedMultiplier() {
        Entity player = PlayerFactory.createPlayer();
        Entity drone = DroneEnemyFactory.createDroneEnemy(singleWaypoint(0.5f), player, Difficulty.EASY);
        assertWaypointSpeed(drone, 0.6f, 1.2f);
    }

    @Test
    void gruntUsesWaypointSpeedMultiplier() {
        Entity player = PlayerFactory.createPlayer();
        Entity grunt = GruntEnemyFactory.createGruntEnemy(singleWaypoint(0.5f), player, Difficulty.EASY);
        assertWaypointSpeed(grunt, 0.4f, 0.8f);
    }

    @Test
    void tankUsesWaypointSpeedMultiplier() {
        Entity player = PlayerFactory.createPlayer();
        Entity tank = TankEnemyFactory.createTankEnemy(singleWaypoint(0.5f), player, Difficulty.EASY);
        assertWaypointSpeed(tank, 0.3f, 0.6f);
    }

    @Test
    void bossUsesWaypointSpeedMultiplier() {
        Entity player = PlayerFactory.createPlayer();
        Entity boss = BossEnemyFactory.createBossEnemy(singleWaypoint(0.5f), player, Difficulty.EASY);
        assertWaypointSpeed(boss, 0.25f, 0.5f);
    }

    private java.util.List<Entity> singleWaypoint(float multiplier) {
        Entity waypoint = new Entity();
        waypoint.addComponent(new SpeedWaypointComponent(multiplier));
        java.util.List<Entity> waypoints = new java.util.ArrayList<>();
        waypoints.add(waypoint);
        return waypoints;
    }

    private void assertWaypointSpeed(Entity enemy, float expectedSpeed, float expectedBaseSpeed) {
        WaypointComponent waypointComponent = enemy.getComponent(WaypointComponent.class);
        assertNotNull(waypointComponent);

        Vector2 currentSpeed = waypointComponent.getSpeed();
        assertEquals(expectedSpeed, currentSpeed.x, 0.0001f);
        assertEquals(expectedSpeed, currentSpeed.y, 0.0001f);

        Vector2 baseSpeed = waypointComponent.getBaseSpeed();
        assertEquals(expectedBaseSpeed, baseSpeed.x, 0.0001f);
        assertEquals(expectedBaseSpeed, baseSpeed.y, 0.0001f);
    }
}
