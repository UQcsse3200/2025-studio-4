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
            // ✅ 用可旋转贴图组件
            .addComponent(new RotatingTextureRenderComponent(cfg.heroTexture))
            .addComponent(new CombatStatsComponent(cfg.health, cfg.baseAttack, resistance, weakness))
            .addComponent(new HeroTurretAttackComponent(
                    cfg.attackCooldown,
                    cfg.bulletSpeed,
                    cfg.bulletLife,
                    cfg.bulletTexture,
                    camera   // ✅ 注入相机供旋转&射击方向计算
            ));

    // 确保有可见尺寸；测试里一般用 (1,1)
    hero.setScale(1f, 1f);
    return hero;
  }
}

