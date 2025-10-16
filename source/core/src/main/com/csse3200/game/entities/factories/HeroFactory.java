package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.hero.HeroAppearanceComponent;
import com.csse3200.game.components.hero.samurai.SamuraiSpinAttackComponent;
import com.csse3200.game.components.hero.HeroUpgradeComponent;
import com.csse3200.game.components.hero.HeroCustomizationComponent;
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

    public static void loadAssets(ResourceService rs, BaseEntityConfig... configs) {
        LinkedHashSet<String> textures  = new LinkedHashSet<>();
        LinkedHashSet<String> soundKeys = new LinkedHashSet<>(); // ★ 声音键集合（方法内局部变量）

        for (BaseEntityConfig base : configs) {
            if (base == null) continue;

            if (base instanceof HeroConfig cfg) {
                if (cfg.heroTexture != null && !cfg.heroTexture.isBlank()) textures.add(cfg.heroTexture);
                if (cfg.levelTextures != null) {
                    for (String s : cfg.levelTextures) if (s != null && !s.isBlank()) textures.add(s);
                }
                if (cfg.bulletTexture != null && !cfg.bulletTexture.isBlank()) textures.add(cfg.bulletTexture);

                // ★ 收集每个形态配置里的射击音效键
                if (cfg.shootSfx != null && !cfg.shootSfx.isBlank()) {
                    soundKeys.add(cfg.shootSfx);
                }

                if (cfg instanceof EngineerConfig ec) {
                    if (ec.summonTexture != null && !ec.summonTexture.isBlank()) textures.add(ec.summonTexture);
                }
            } else if (base instanceof SamuraiConfig sc) {
                if (sc.heroTexture != null && !sc.heroTexture.isBlank()) textures.add(sc.heroTexture);
                if (sc.levelTextures != null) {
                    for (String s : sc.levelTextures) if (s != null && !s.isBlank()) textures.add(s);
                }
                if (sc.swordTexture  != null && !sc.swordTexture.isBlank())  textures.add(sc.swordTexture);
                if (sc.swordTexture2 != null && !sc.swordTexture2.isBlank()) textures.add(sc.swordTexture2);
                if (sc.swordTexture3 != null && !sc.swordTexture3.isBlank()) textures.add(sc.swordTexture3);
            }

        }

        if (!textures.isEmpty()) {
            rs.loadTextures(textures.toArray(new String[0]));
        }
        if (!soundKeys.isEmpty()) { // ★ 别忘了把声音也加载
            rs.loadSounds(soundKeys.toArray(new String[0]));
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
     * @param cfg    hero configuration (stats, cooldown, bullet properties, textures)
     * @param camera the active game camera (used for aiming and rotation)
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
                .addComponent(new RotatingTextureRenderComponent(cfg.heroTexture))
                .addComponent(new CombatStatsComponent(cfg.health, cfg.baseAttack, resistance, weakness))
                .addComponent(new HeroTurretAttackComponent(
                        cfg.attackCooldown,
                        cfg.bulletSpeed,
                        cfg.bulletLife,
                        cfg.bulletTexture,
                        camera
                ))
                .addComponent(new HeroUpgradeComponent())
                .addComponent(new HeroUltimateComponent())
                .addComponent(new UltimateButtonComponent())
                .addComponent(new HeroAppearanceComponent(cfg))
                .addComponent(new HeroCustomizationComponent());

        hero.setScale(1f, 1f);

        HeroTurretAttackComponent atk = hero.getComponent(HeroTurretAttackComponent.class);
        if (atk != null) {
            if (cfg.shootSfx != null && !cfg.shootSfx.isBlank()) {
                atk.setShootSfxKey(cfg.shootSfx);
            }
            if (cfg.shootSfxVolume != null) {
                atk.setShootSfxVolume(Math.max(0f, Math.min(1f, cfg.shootSfxVolume)));
            }
        }

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

    // HeroFactory.java
    public static Entity createHeroGhost(String heroTexture, float alpha) {
        Entity e = new Entity();
        TextureRenderComponent tex = new TextureRenderComponent(
                (heroTexture != null && !heroTexture.isBlank())
                        ? heroTexture
                        : "images/hero/Heroshoot.png" // fallback
        );
        tex.setColor(new Color(1f, 1f, 1f, Math.max(0f, Math.min(alpha, 1f))));
        e.addComponent(tex);
        // 不加任何 Physics/Collider/Hitbox/攻击/升级组件，避免警告和性能开销
        return e;
    }



    // === HeroFactory.createEngineerHero(...) ===

    /**
     * Creates and configures an Engineer hero entity.
     * <p>
     * The Engineer specializes in deploying turrets, melee bots, and resource-generating summons.
     * This factory method assembles the hero with its necessary gameplay components, such as
     * summoning logic, ranged attack, upgrade, and ultimate abilities.
     * </p>
     *
     * @param cfg    The {@link EngineerConfig} containing hero stats and asset references.
     * @param camera The active camera, used for attack targeting or rotation.
     * @return A fully initialized Engineer hero entity.
     */
    public static Entity createEngineerHero(EngineerConfig cfg, Camera camera) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        Entity hero = new Entity()
                // === Core physics & collision setup ===
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))

                // === Rendering ===
                .addComponent(new RotatingTextureRenderComponent(cfg.heroTexture))

                // === Core stats ===
                .addComponent(new CombatStatsComponent(cfg.health, cfg.baseAttack, resistance, weakness))

                // === Engineer’s summon logic ===
                .addComponent(new EngineerSummonComponent(
                        cfg.summonCooldown,
                        cfg.maxSummons,
                        cfg.summonTexture,
                        new Vector2(cfg.summonSpeed, cfg.summonSpeed)
                ));

                // === Optional: Engineer’s ranged turret attack ===
                // Handles rotating aim and shooting bullets toward cursor direction
        HeroTurretAttackComponent atc = new HeroTurretAttackComponent(
                cfg.attackCooldown,
                cfg.bulletSpeed,
                cfg.bulletLife,
                cfg.bulletTexture, // Engineer-specific bullet texture
                camera
        );
        atc.setShootSfxKey(
                (cfg.shootSfx != null && !cfg.shootSfx.isBlank())
                        ? cfg.shootSfx
                        : "sounds/Explosion_sfx.ogg"     // 兜底
        ).setShootSfxVolume(0.9f);              // 可选：音量 0~1

        hero.addComponent(atc)

                // === Hero upgrade and ultimate systems ===
                .addComponent(new HeroUpgradeComponent())
                .addComponent(new HeroUltimateComponent())

                // === Visual appearance customization ===
                .addComponent(new HeroAppearanceComponent(cfg));

        hero.setScale(1f, 1f);
        return hero;
    }

// Required imports:
// SamuraiConfig, PhysicsLayer, PhysicsComponent, ColliderComponent, HitboxComponent,
// RotatingTextureRenderComponent, CombatStatsComponent, DamageTypeConfig, Camera, Entity, etc.


    /**
     * Creates and configures a Samurai hero entity.
     * <p>
     * The Samurai specializes in close-range melee combat using a spinning sword
     * that orbits around the hero. This factory method assembles the hero with all
     * required gameplay and rendering components.
     * </p>
     *
     * <ul>
     *   <li>Includes sword rotation via {@link SamuraiSpinAttackComponent}.</li>
     *   <li>Centers the sword’s visual origin to ensure smooth rotation.</li>
     *   <li>Supports fine pivot adjustments for precise visual alignment.</li>
     * </ul>
     *
     * @param cfg    The {@link SamuraiConfig} containing hero stats and asset paths.
     * @param camera The active camera for rendering or camera-relative effects.
     * @return A fully initialized Samurai hero entity.
     */
    public static Entity createSamuraiHero(SamuraiConfig cfg, Camera camera) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        RotatingTextureRenderComponent body = new RotatingTextureRenderComponent(cfg.heroTexture);

        Entity hero = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
                .addComponent(body)
                .addComponent(new CombatStatsComponent(cfg.health, cfg.baseAttack, resistance, weakness))
                .addComponent(
                        new com.csse3200.game.components.hero.samurai.SamuraiSpinAttackComponent(
                                cfg.swordRadius,
                                cfg.swordTexture,
                                cfg,
                                camera
                        )
                                .setSpriteForwardOffsetDeg(0f)
                                .setCenterToHandle(-0.25f)
                                .setJabParams(0.18f, 0.8f, 0.00f)
                                .setSweepParams(0.22f, 0.35f, 0.00f)
                )
                .addComponent(new HeroUpgradeComponent())
                .addComponent(new HeroUltimateComponent());

        hero.setScale(1f, 1f);

        try {
            body.getClass().getMethod("setOriginToCenter").invoke(body);
        } catch (Throwable ignore) {
            try {
                float w = (float) body.getClass().getMethod("getTextureWidth").invoke(body);
                float h = (float) body.getClass().getMethod("getTextureHeight").invoke(body);
                body.getClass().getMethod("setOrigin", float.class, float.class).invoke(body, w / 2f, h / 2f);
            } catch (Throwable ignore2) {}
        }

        // ★ 开局按用户设置应用一次武器外观（默认 Normal Sword）
        String initial = "Normal Sword";
        try {
            var us = com.csse3200.game.files.UserSettings.get();
            if (us != null && us.heroWeapon != null && !us.heroWeapon.isBlank()) {
                initial = us.heroWeapon;
            }
        } catch (Throwable ignored) {}
        hero.getEvents().trigger("samurai:weapon:set", initial);

        return hero;
    }




}