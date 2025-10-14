package com.csse3200.game.entities.factories;

import com.csse3200.game.components.HealthBarComponent;
import com.csse3200.game.components.HomebaseDamageEffectComponent;
import com.csse3200.game.components.HomebaseHealingEffectComponent;
import com.csse3200.game.components.HomebaseRegenerationComponent;
import com.csse3200.game.components.PlayerCombatStatsComponent;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.deck.DeckDisplay;
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
import com.csse3200.game.components.PlayerScoreComponent;
import com.csse3200.game.components.PlayerRankingComponent;
import com.csse3200.game.ui.DamagePopupComponent;

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
    return createPlayer("images/basement.png");
  }

  /**
   * Create a basement entity with a custom texture.
   * @param texturePath the path to the homebase texture
   * @return a new basement entity
   */
  public static Entity createPlayer(String texturePath) {
    return createPlayer(texturePath, BASEMENT_SCALE);
  }

  /**
   * Create a basement entity with a custom texture and custom scale.
   * @param texturePath the path to the homebase texture
   * @param scale the scale factor for the homebase (1.0 = original size)
   * @return a new basement entity
   */
  public static Entity createPlayer(String texturePath, float scale) {
    return createPlayer(texturePath, scale, new HealthBarComponent());
  }

  /**
   * Create a basement entity with custom texture, scale, and health bar configuration.
   * @param texturePath the path to the homebase texture
   * @param scale the scale factor for the homebase (1.0 = original size)
   * @param healthBar custom health bar component
   * @return a new basement entity
   */
  public static Entity createPlayer(String texturePath, float scale, HealthBarComponent healthBar) {
    InputComponent inputComponent =
            ServiceLocator.getInputService().getInputFactory().createForPlayer();

    Entity basement = new Entity()
            .addComponent(new SwitchableTextureRenderComponent(texturePath))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setSensor(true))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
            .addComponent(new PlayerActions())
            .addComponent(new PlayerCombatStatsComponent(stats.health, stats.baseAttack))
            .addComponent(new InventoryComponent(stats.gold))
            .addComponent(inputComponent)
            .addComponent(new CurrencyManagerComponent())
            .addComponent(new PlayerStatsDisplay())
            .addComponent(new HomebaseDamageEffectComponent(texturePath))
            .addComponent(new HomebaseHealingEffectComponent())
            .addComponent(new HomebaseRegenerationComponent())
            .addComponent(new PlayerScoreComponent())
            .addComponent(new DamagePopupComponent())
            .addComponent(healthBar)
            .addComponent(new PlayerRankingComponent())
            .addComponent(new HealthBarComponent())
            .addComponent(new DeckDisplay());

    // 先设置显示尺寸，再按比例设置碰撞体，确保碰撞体随缩放一起变大
    basement.setScale(scale, scale);
    PhysicsUtils.setScaledCollider(basement, 0.6f, 0.3f);
    basement.getComponent(ColliderComponent.class).setDensity(1.5f);
    var gs = ServiceLocator.getGameStateService();
    if (gs != null) {
      gs.setBase(basement);
    }
    return basement;
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}