package com.csse3200.game.entities;

import com.csse3200.game.components.PlayerCombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class PlayerFactoryBasementTest {

  @BeforeEach
  void setup() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());
  }

  @Test
  void basement_hasComponentsAndScaledCollider() {
    Entity basement = PlayerFactory.createPlayer();

    // 新功能1：有贴图与尺寸缩放
    assertNotNull(basement.getComponent(TextureRenderComponent.class));
    assertTrue(basement.getScale().x > 1f && basement.getScale().y > 1f);

    // 新功能2：物理与碰撞体（传感器Collider + 玩家Hitbox）
    assertNotNull(basement.getComponent(PhysicsComponent.class));
    assertNotNull(basement.getComponent(ColliderComponent.class));
    assertNotNull(basement.getComponent(HitboxComponent.class));

    // 新功能3：玩家属性和货币系统
    assertNotNull(basement.getComponent(PlayerCombatStatsComponent.class));
    assertNotNull(basement.getComponent(InventoryComponent.class));
    assertNotNull(basement.getComponent(CurrencyManagerComponent.class));
  }
}


