package com.csse3200.game.entities.factories;

import com.csse3200.game.components.HomebaseDamageEffectComponent;
import com.csse3200.game.components.PlayerCombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.PlayerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.SwitchableTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Basement factory (renamed from PlayerFactory).
 * Loads original player stats from configs/player.json.
 */
public class PlayerFactory {
  private static final PlayerConfig stats =
          FileLoader.readClass(PlayerConfig.class, "configs/player.json");

  // 可调：Basement在世界中的渲染缩放（1=原始大小）。
  private static final float BASEMENT_SCALE = 1.88f;

  /**
   * Create a basement entity that inherits original player attributes (health, inventory, currency).
   */
  public static Entity createPlayer() {
    InputComponent inputComponent =
            ServiceLocator.getInputService().getInputFactory().createForPlayer();

    Entity basement = new Entity()
            .addComponent(new SwitchableTextureRenderComponent("images/basement.png"))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setSensor(true))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
            .addComponent(new PlayerActions())
            .addComponent(new PlayerCombatStatsComponent(stats.health, stats.baseAttack))
            .addComponent(new InventoryComponent(stats.gold))
            .addComponent(inputComponent)
            .addComponent(new PlayerStatsDisplay())
            .addComponent(new CurrencyManagerComponent())
            .addComponent(new HomebaseDamageEffectComponent());

    // 先设置显示尺寸，再按比例设置碰撞体，确保碰撞体随缩放一起变大
    basement.setScale(BASEMENT_SCALE, BASEMENT_SCALE);
    PhysicsUtils.setScaledCollider(basement, 0.6f, 0.3f);
    basement.getComponent(ColliderComponent.class).setDensity(1.5f);
    return basement;
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}

