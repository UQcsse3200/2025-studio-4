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
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.components.hero.HeroTurretAttackComponent;

public final class HeroFactory {
  private HeroFactory() { throw new IllegalStateException("Instantiating static util class"); }

  /** 用配置构建固定炮台英雄（无移动，只射击） */
  public static Entity createHero(HeroConfig cfg, Camera camera) {
    var resistance = DamageTypeConfig.None;
    var weakness   = DamageTypeConfig.None;

    Entity hero = new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
            .addComponent(new TextureRenderComponent(cfg.heroTexture))
            .addComponent(new CombatStatsComponent(cfg.health, cfg.baseAttack, resistance, weakness))
            .addComponent(new HeroTurretAttackComponent(
                    cfg.attackCooldown,
                    cfg.bulletSpeed,
                    cfg.bulletLife,
                    cfg.bulletTexture,
                    camera   // ✅ 注入相机
            ));
    TextureRenderComponent tex = hero.getComponent(TextureRenderComponent.class);
    if (tex != null) {
      // 这里如果有 setOriginCenter() 方法就用，没有的话不用写，
      // 因为我们在 draw() 已经指定绕中心旋转了
      // tex.setOriginCenter();
    }

    return hero;
  }
}
