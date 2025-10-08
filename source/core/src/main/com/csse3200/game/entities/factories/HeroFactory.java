package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.hero.HeroAppearanceComponent;
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
import com.csse3200.game.ui.UltimateButtonComponent;

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
                                  HeroConfig... configs) { // 可接收 EngineerConfig 等各种子类
        LinkedHashSet<String> textures = new LinkedHashSet<>();
        for (HeroConfig cfg : configs) {
            if (cfg == null) continue;
            if (cfg.heroTexture != null && !cfg.heroTexture.isBlank()) textures.add(cfg.heroTexture);
            if (cfg.levelTextures != null) {
                for (String s : cfg.levelTextures) if (s != null && !s.isBlank()) textures.add(s);
            }
            if (cfg.bulletTexture != null && !cfg.bulletTexture.isBlank()) textures.add(cfg.bulletTexture);

            // EngineerConfig 额外字段
            if (cfg instanceof EngineerConfig ec) {
                if (ec.summonTexture != null && !ec.summonTexture.isBlank()) textures.add(ec.summonTexture);
            }
        }

        if (!textures.isEmpty()) {
            rs.loadTextures(textures.toArray(new String[0]));
        }
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
                .addComponent(new UltimateButtonComponent())
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
                .addComponent(new UltimateButtonComponent())
                .addComponent(new HeroAppearanceComponent(cfg));

        hero.setScale(1f, 1f);
        return hero;
    }


}




