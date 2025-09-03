package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Camera;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.HeroConfig;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.components.hero.HeroTurretAttackComponent;

/**
 * Factory class for creating hero entities.
 * <p>
 * Provides a method to construct a stationary "turret-style" hero:
 * - Has no movement logic.
 * - Can rotate to face the mouse cursor.
 * - Fires bullets in the aimed direction using {@link HeroTurretAttackComponent}.
 * </p>
 */
public final class HeroFactory {
  private HeroFactory() { throw new IllegalStateException("Instantiating static util class"); }

  /**
   * Create a hero entity based on a {@link HeroConfig}.
   *
   * @param cfg     hero configuration (stats, cooldown, bullet properties, textures)
   * @param camera  the active game camera (used for aiming )
   * @return a new hero entity configured as a turret-style shooter
   */
  public static Entity createHero(HeroConfig cfg, Camera camera) {
    var resistance = DamageTypeConfig.None;
    var weakness   = DamageTypeConfig.None;

    Entity hero = new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
            // Renderable texture with rotation support
            .addComponent(new RotatingTextureRenderComponent(cfg.heroTexture))
            // Combat stats (health and base attack)
            .addComponent(new CombatStatsComponent(cfg.health, cfg.baseAttack, resistance, weakness))
            // Turret attack logic (fires bullets at mouse direction)
            .addComponent(new HeroTurretAttackComponent(
                    cfg.attackCooldown,
                    cfg.bulletSpeed,
                    cfg.bulletLife,
                    cfg.bulletTexture,
                    camera // Inject camera for rotation & aiming
            ));

    // Ensure the entity has a visible scale (default to 1x1 for testing)
    hero.setScale(1f, 1f);
    return hero;
  }
}


