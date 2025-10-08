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
                ))

                // === Optional: Engineer’s ranged turret attack ===
                // Handles rotating aim and shooting bullets toward cursor direction
                .addComponent(new HeroTurretAttackComponent(
                        cfg.attackCooldown,
                        cfg.bulletSpeed,
                        cfg.bulletLife,
                        cfg.bulletTexture, // Engineer-specific bullet texture
                        camera
                ))

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

        // (1) Create a shared rendering component (avoid creating duplicates later)
        RotatingTextureRenderComponent body = new RotatingTextureRenderComponent(cfg.heroTexture);

        Entity hero = new Entity()
                // === Core components ===
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setSensor(true))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))

                // === Rendering ===
                .addComponent(body) // Only one rendering component is needed

                // === Combat stats ===
                .addComponent(new CombatStatsComponent(cfg.health, cfg.baseAttack, resistance, weakness))

                // === Sword attack logic (rotation & collision) ===
                .addComponent(new SamuraiSpinAttackComponent(
                        cfg.swordRadius,   // Sword orbit radius
                        cfg.swordTexture,  // Sword texture
                        cfg,
                        camera             // Camera reference
                ))

                // === Upgrade and ultimate abilities ===
                .addComponent(new HeroUpgradeComponent())
                .addComponent(new HeroUltimateComponent())
                .addComponent(new HeroAppearanceComponent(cfg));

        hero.setScale(1f, 1f);

        // (2) Attempt to set the texture’s origin to its center
        //     This ensures the Samurai rotates around its middle instead of the bottom-left corner.
        try {
            // Common case: directly supported by RotatingTextureRenderComponent
            body.getClass().getMethod("setOriginToCenter").invoke(body);
        } catch (Throwable ignore) {
            try {
                // Fallback: manually calculate the center point using width/height
                float w = (float) body.getClass().getMethod("getTextureWidth").invoke(body);
                float h = (float) body.getClass().getMethod("getTextureHeight").invoke(body);
                body.getClass().getMethod("setOrigin", float.class, float.class).invoke(body, w / 2f, h / 2f);
            } catch (Throwable ignore2) {
                // If the render component doesn’t support setting origin,
                // the sword’s orbit pivot can be adjusted manually (see step 3 below)
            }
        }

        // (3) Optional fine-tuning: adjust sword rotation alignment if visuals look off-center
        SamuraiSpinAttackComponent spin = hero.getComponent(SamuraiSpinAttackComponent.class);
        if (spin != null) {
            // Controls which direction the sword texture faces by default:
            // → = 0°, ↑ = 90°, ← = 180°, ↓ = 270°
            spin.setSpriteForwardOffsetDeg(0f);

            // If the sword still appears offset, adjust its orbit pivot:
            // Retrieve the SwordOrbitPhysicsComponent (within SamuraiSpinAttackComponent)
            // and call orbit.setPivotOffset(ox, oy), e.g., (0f, 0f) or slightly adjusted.
            // Example:
            // orbit.setPivotOffset(0.1f, -0.1f);
        }

        return hero;
    }





}




