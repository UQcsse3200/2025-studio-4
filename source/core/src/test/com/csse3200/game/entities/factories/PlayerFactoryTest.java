package com.csse3200.game.entities.factories;

import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(GameExtension.class)
public class PlayerFactoryTest {

    // Setup before any tests
    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerResourceService(resourceService);

        // Load assets needed for PlayerFactory
        resourceService.loadTextures(new String[]{"images/basement.png", "images/grunt_enemy.png", "images/boss_enemy.png", "images/drone_enemy.png", "images/tank_enemy.png"});
        resourceService.loadAll();
    }

    // Player must have PlayerScoreComponent added upon creation. If null, cannot award points.
    @Test
    void playerHasScoreComponent() {
        Entity player = PlayerFactory.createPlayer();
        assertNotNull(player.getComponent(PlayerScoreComponent.class));
    }
}
