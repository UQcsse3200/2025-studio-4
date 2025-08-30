package com.csse3200.game.entities.configs;

/**
 * 英雄的数值与资源配置（可由 FileLoader 从 JSON 读取）
 * 继承 BaseEntityConfig，包含 health 与 baseAttack。
 */
public class HeroConfig extends BaseEntityConfig {
  /** 移动速度（像素/秒） */
  public float moveSpeed = 220f;

  /** 攻击冷却（秒/次）——越小攻速越快 */
  public float attackCooldown = 0.15f;

  /** 子弹寿命（秒）与速度（像素/秒） */
  public float bulletLife = 1.6f;
  public float bulletSpeed = 900f;

  /** 资源路径（与 assets 对齐） */
  public String heroTexture = "images/hero/Heroshoot.png";
  public String bulletTexture = "images/hero/Bullet.png";

  /** 默认构造：BaseEntityConfig 提供的通用字段 */
  public HeroConfig() {
    // BaseEntityConfig 默认：
    // health = 1; baseAttack = 0;
    // 这里可按需覆盖默认值（示例给出更合理的英雄初始值）
    this.health = 100;
    this.baseAttack = 20;
  }
}
