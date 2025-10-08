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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(GameExtension.class)
public class EnemySpeedWaypointTest {

    @BeforeEach
    void beforeEach() {
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

        GruntEnemyFactory.resetToDefaults();
    }

    @Test
    void gruntEnemySlowsDownAtSpeedWaypoint() {
        Entity player = PlayerFactory.createPlayer();

        Entity slowWaypoint = new Entity();
        slowWaypoint.addComponent(new SpeedWaypointComponent(0.5f));
        slowWaypoint.create();

        List<Entity> waypoints = new ArrayList<>();
        waypoints.add(slowWaypoint);

        Entity grunt = GruntEnemyFactory.createGruntEnemy(waypoints, player, Difficulty.EASY);
        WaypointComponent waypointComponent = grunt.getComponent(WaypointComponent.class);

        assertNotNull(waypointComponent);
        Vector2 expectedSpeed = new Vector2(0.4f, 0.4f);
        assertEquals(expectedSpeed, waypointComponent.getSpeed());
    }
}
