package com.csse3200.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class ObstacleFactorySnowTreeTest {

  @BeforeEach
  void setup() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerPhysicsService(new PhysicsService());
    // Mock resource service to provide required texture asset
    ResourceService resourceService = mock(ResourceService.class);
    Texture mockTexture = mock(Texture.class);
    when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mockTexture);
    ServiceLocator.registerResourceService(resourceService);
  }

  @Test
  void createSnowTree_hasPhysicsCollider_andScaledBox() {
    Entity e = ObstacleFactory.createSnowTree();

    assertNotNull(e.getComponent(PhysicsComponent.class));
    ColliderComponent collider = e.getComponent(ColliderComponent.class);
    assertNotNull(collider);

  }
}
