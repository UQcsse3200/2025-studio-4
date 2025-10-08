package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.hero.HeroAppearanceComponent;
import com.csse3200.game.components.hero.samurai.SamuraiSpinAttackComponent;
import com.csse3200.game.components.hero.HeroUpgradeComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.*;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.rendering.RotatingTextureRenderComponent;
import com.csse3200.game.components.hero.HeroTurretAttackComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.hero.engineer.EngineerSummonComponent;

import java.util.LinkedHashSet;
import com.csse3200.game.components.hero.HeroUltimateComponent;

/**
 * Factory class for creating hero entities.
 * <p>
 * Provides methods for constructing:
 * <ul>
 *   <li>A fully functional turret-style hero that can rotate and shoot.</li>
 *   <li>A "ghost hero" used for placement previews (render-only, no logic).</li>
 * </ul>
 */
public final class HeroFactory {
    private HeroFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    public static void loadAssets(ResourceService rs,
                                  BaseEntityConfig... configs) {
        LinkedHashSet<String> textures = new LinkedHashSet<>();
        for (BaseEntityConfig base : configs) {
            if (base == null) continue;

            if (base instanceof HeroConfig cfg) {
                if (cfg.heroTexture != null && !cfg.heroTexture.isBlank()) textures.add(cfg.heroTexture);
                if (cfg.levelTextures != null) {
                    for (String s : cfg.levelTextures) if (s != null && !s.isBlank()) textures.add(s);
                }
                if (cfg.bulletTexture != null && !cfg.bulletTexture.isBlank()) textures.add(cfg.bulletTexture);

                if (cfg instanceof EngineerConfig ec) {
                    if (ec.summonTexture != null && !ec.summonTexture.isBlank()) textures.add(ec.summonTexture);
                }
            } else if (base instanceof SamuraiConfig sc) {
                if (sc.heroTexture != null && !sc.heroTexture.isBlank()) textures.add(sc.heroTexture);
                if (sc.levelTextures != null) {
                    for (String s : sc.levelTextures) if (s != null && !s.isBlank()) textures.add(s);
                }
                if (sc.swordTexture != null && !sc.swordTexture.isBlank()) textures.add(sc.swordTexture);
            }
            // 未来还要加别的职业/形态就在这里继续 instanceof ...
        }
        if (!textures.isEmpty()) rs.loadTextures(textures.toArray(new String[0]));
    }



    /**
     * Create a hero entity based on a {@link HeroConfig}.
     * <p>
     * The hero is built as a stationary, turret-style shooter:
     * <ul>
     *   <li>Includes a rotating render component.</li>
     *   <li>Has combat stats and hitbox on the PLAYER layer.</li>
     *   <li>Can aim toward the mouse cursor and fire bullets using
     *       {@link HeroTurretAttackComponent}.</li>
     * </ul>
     *
     * @param cfg     hero configuration (stats, cooldown, bullet properties, textures)
     * @param camera  the active game camera (used for aiming and rotation)
     * @return a new hero entity configured as a turret-style shooter
     */
    public static Entity createHero(HeroConfig cfg, Camera camera) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        // Initialize the Hero entity
        Entity hero = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
                // Renderable texture with rotation support
                .addComponent(new RotatingTextureRenderComponent(cfg.heroTexture))
                // Combat stats (health, attack, resistances/weaknesses)
                .addComponent(new CombatStatsComponent(cfg.health, cfg.baseAttack, resistance, weakness))
                // Turret attack logic (fires bullets toward mouse cursor)
                .addComponent(new HeroTurretAttackComponent(
                        cfg.attackCooldown,
                        cfg.bulletSpeed,
                        cfg.bulletLife,
                        cfg.bulletTexture, // Bullet texture is passed directly from HeroConfig
                        camera // Inject camera for aiming & rotation
                ))
                .addComponent(new HeroUpgradeComponent())
                .addComponent(new HeroUltimateComponent())
                .addComponent(new HeroAppearanceComponent(cfg));

        // Default scale to 1x1 so the hero is visible during testing
        hero.setScale(1f, 1f);
        return hero;
    }

    /**
     * Create a "ghost hero" entity used only for placement previews.
     * <p>
     * Characteristics:
     * <ul>
     *   <li>Contains only a {@link TextureRenderComponent}.</li>
     *   <li>Renders semi-transparently using the provided alpha value.</li>
     *   <li>No physics, collisions, AI, or attack logic.</li>
     *   <li>Not registered to any physics layer.</li>
     * </ul>
     *
     * @param alpha Transparency (0–1). Recommended values: 0.4–0.6
     * @return a new ghost hero entity (render-only)
     */
    public static Entity createHeroGhost(float alpha) {
        Entity e = new Entity();

        TextureRenderComponent texture =
                new TextureRenderComponent("images/hero/Heroshoot.png");
        texture.setColor(new Color(1f, 1f, 1f, Math.max(0f, Math.min(alpha, 1f))));

        e.addComponent(texture);
        return e;
    }


    // HeroFactory.createEngineerHero(...)
    public static Entity createEngineerHero(EngineerConfig cfg, Camera camera) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        Entity hero = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
                .addComponent(new RotatingTextureRenderComponent(cfg.heroTexture))
                .addComponent(new CombatStatsComponent(cfg.health, cfg.baseAttack, resistance, weakness))
                // ⭐ 工程师召唤逻辑
                .addComponent(new EngineerSummonComponent(
                        cfg.summonCooldown,
                        cfg.maxSummons,
                        cfg.summonTexture,
                        new Vector2(cfg.summonSpeed, cfg.summonSpeed)
                ))
                // ⭐ 额外挂上 HeroTurretAttackComponent 负责旋转（可以同时射击）
                .addComponent(new HeroTurretAttackComponent(
                        cfg.attackCooldown,
                        cfg.bulletSpeed,
                        cfg.bulletLife,
                        cfg.bulletTexture,  // 你可以让工程师也有一套子弹贴图
                        camera
                ))
                .addComponent(new HeroUpgradeComponent())
                .addComponent(new HeroUltimateComponent())
                .addComponent(new HeroAppearanceComponent(cfg));

        hero.setScale(1f, 1f);
        return hero;
    }

    // imports 需要：SamuraiConfig, PhysicsLayer, PhysicsComponent, ColliderComponent, HitboxComponent,
// RotatingTextureRenderComponent, CombatStatsComponent, DamageTypeConfig, Camera, Entity 等

    public static Entity createSamuraiHero(SamuraiConfig cfg, Camera camera) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        // 1) 先构造渲染组件，并只用这一份（不要后面再 new 一份）
        RotatingTextureRenderComponent body = new RotatingTextureRenderComponent(cfg.heroTexture);

        Entity hero = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
                .addComponent(body) // ← 只加这一份渲染组件
                .addComponent(new CombatStatsComponent(cfg.health, cfg.baseAttack, resistance, weakness))
                .addComponent(new SamuraiSpinAttackComponent(
                        cfg.swordRadius,   // 直接传半径
                        cfg.swordTexture,  // 贴图
                        cfg,
                        camera             // 相机
                ))
                .addComponent(new HeroUpgradeComponent())
                .addComponent(new HeroUltimateComponent())
                .addComponent(new HeroAppearanceComponent(cfg));

        hero.setScale(1f, 1f);

        // 2) 把贴图原点设到中心（如果组件支持；避免“绕左下角转”的问题）
        try {
            // 常见封装：有就直接用
            body.getClass().getMethod("setOriginToCenter").invoke(body);
        } catch (Throwable ignore) {
            try {
                // 次优方案：用宽高的一半作为原点
                float w = (float) body.getClass().getMethod("getTextureWidth").invoke(body);
                float h = (float) body.getClass().getMethod("getTextureHeight").invoke(body);
                body.getClass().getMethod("setOrigin", float.class, float.class).invoke(body, w / 2f, h / 2f);
            } catch (Throwable ignore2) {
                // 如果你的渲染组件完全不支持设置 origin，就在剑的轨道组件上做“圆心偏移”补偿（见下方 3）
            }
        }

        // 3) （可选但推荐）如果你无法设置 origin，或看起来还不居中：给剑的轨道加 pivotOffset 进行微调
        SamuraiSpinAttackComponent spin = hero.getComponent(SamuraiSpinAttackComponent.class);
        if (spin != null) {
            // 让 sword 围绕“我们想要的可视中心”来转：
            // 如果实体 position 是“左下角”，而 getCenterPosition() 用的是 pos + scale/2，
            // 且你的贴图实际中心≠ scale/2，可以用 pivotOffset 细调（比如 0.5,0.5 是 1x1 世界单位贴图的中心）
            // 先从 (0,0) 开始，如果仍偏，就一点点调：
            spin.setSpriteForwardOffsetDeg(0f);    // 贴图默认朝向：→=0, ↑=90, ←=180, ↓=270
            // 如果还偏移，再到 SwordOrbitPhysicsComponent 上调用 setPivotOffset(ox, oy)（见下行注释）
            // 在 SamuraiSpinAttackComponent 里你是通过 SwordFactory/Orbit 组件构建的：
            // 取到 orbit 组件后：orbit.setPivotOffset(ox, oy);  先试 (0,0)，不行再微调
        }

        return hero;
    }





}




