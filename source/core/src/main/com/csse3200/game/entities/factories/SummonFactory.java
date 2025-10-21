package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.hero.engineer.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.DamageTypeConfig;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory class for creating Engineer-related summon entities such as:
 * <ul>
 *   <li>Melee summons (e.g., drones or barricades)</li>
 *   <li>Directional turrets</li>
 *   <li>Currency-generating bots</li>
 *   <li>Ghost previews for placement</li>
 * </ul>
 * <p>
 * All entities created here are preconfigured with the appropriate physics,
 * rendering, and combat components.
 * </p>
 */
public final class SummonFactory {
    private SummonFactory() {
        // Prevent instantiation (utility factory class)
    }

    // === Melee Summon ===

    /**
     * Creates a simple melee summon (e.g., a close-range bot or barricade).
     * <p>
     * These entities are static (non-moving) and deal touch damage upon contact.
     * They automatically despawn when their HP reaches zero.
     * </p>
     *
     * @param texturePath    Path to the summon texture.
     * @param colliderSensor Whether the collider should act as a sensor (non-solid).
     * @param scale          Scale factor for the summon sprite.
     * @return The created melee summon entity.
     */
    public static Entity createMeleeSummon(String texturePath, boolean colliderSensor, float scale) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        Entity s = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent()
                        .setSensor(false)
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new HitboxComponent()
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new TextureRenderComponent(texturePath))
                .addComponent(new CombatStatsComponent(100, 0, resistance, weakness))
                .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, 4.0f))
                .addComponent(new AutoDespawnOnDeathComponent())// ✅ Auto-despawn on death
                .addComponent(new SfxOnDeathComponent("sounds/explosion_2s.ogg", 0.9f));

        var phys = s.getComponent(PhysicsComponent.class);
        if (phys != null) {
            phys.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);
        }

        PhysicsUtils.setScaledCollider(s, 0.9f * scale, 0.9f * scale);
        s.setScale(scale, scale);
        return s;
    }


    // === Directional Turret ===

    /**
     * Creates a directional turret that fires projectiles in a fixed direction.
     * <p>
     * The turret uses a {@link TurretAttackComponent} for firing bullets and
     * a {@link FourWayCycleComponent} to automatically rotate its attack direction
     * (Up → Right → Down → Left).
     * </p>
     *
     * @param texturePath    Path to the turret texture.
     * @param scale          Scale factor for the turret sprite.
     * @param attackCooldown Time interval (in seconds) between attacks.
     * @param fireDirection  Initial firing direction.
     * @return The created turret entity.
     */
    public static Entity createDirectionalTurret(String texturePath, float scale,
                                                 float attackCooldown, Vector2 fireDirection) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        Entity turret = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent()
                        .setSensor(false)
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new HitboxComponent()
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new TextureRenderComponent(texturePath))
                .addComponent(new CombatStatsComponent(20, 25, resistance, weakness))
                .addComponent(new AutoDespawnOnDeathComponent())// ✅ Auto-despawn on death
                .addComponent(new SfxOnDeathComponent("sounds/explosion_2s.ogg", 0.9f));

        var phys = turret.getComponent(PhysicsComponent.class);
        if (phys != null) {
            phys.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);
        }

        PhysicsUtils.setScaledCollider(turret, 0.8f * scale, 0.8f * scale);
        turret.setScale(scale, scale);

        // Attack behavior: shoot bullets and rotate firing direction cyclically

        turret.addComponent(new FourWayCycleComponent(attackCooldown, fireDirection));
        // 先创建组件
        TurretAttackComponent tac = new TurretAttackComponent(
                fireDirection.nor(), attackCooldown, 10f, 1.2f, "images/engineer/Turret_Bullet.png"
        ).setShootSfxKey("sounds/turret_shoot.ogg")  // ★ 音效路径（放你项目的 assets）
                .setShootSfxVolume(0.9f)                    // 可调
                .setShootSfxMinInterval(0.05f);             // 可调

        turret.addComponent(tac);

        return turret;
    }

    // === Currency Generator Bot ===

    /**
     * Creates a currency-generating robot that periodically produces in-game currency
     * for its owner (typically the player).
     * <p>
     * Includes {@link OwnerComponent} to link it to its summoner and
     * {@link CurrencyGeneratorComponent} to handle timed currency production.
     * </p>
     *
     * @param texturePath   Path to the robot texture.
     * @param scale         Scale factor for the sprite.
     * @param owner         The player entity that owns this bot.
     * @param currencyType  Type of currency generated (e.g., METAL_SCRAP).
     * @param amountPerTick Amount of currency produced each cycle.
     * @param intervalSec   Time interval (in seconds) between generations.
     * @return The created currency bot entity.
     */
    public static Entity createCurrencyBot(
            String texturePath, float scale,
            Entity owner,
            com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType currencyType,
            int amountPerTick, float intervalSec
    ) {
        var resistance = DamageTypeConfig.None;
        var weakness = DamageTypeConfig.None;

        Entity bot = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent()
                        .setSensor(false)
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new HitboxComponent()
                        .setLayer(PhysicsLayer.PLAYER))
                .addComponent(new TextureRenderComponent(texturePath))
                .addComponent(new CombatStatsComponent(5, 0, resistance, weakness)) // Minimal HP
                .addComponent(new AutoDespawnOnDeathComponent())
                .addComponent(new SfxOnDeathComponent("sounds/explosion_2s.ogg", 0.9f))
                .addComponent(new OwnerComponent(owner))
                .addComponent(new CurrencyGeneratorComponent(owner, currencyType, amountPerTick, intervalSec));

        var phys = bot.getComponent(PhysicsComponent.class);
        if (phys != null) {
            phys.setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);
        }

        PhysicsUtils.setScaledCollider(bot, 0.8f * scale, 0.8f * scale);
        bot.setScale(scale, scale);

        return bot;
    }

    // === Ghost Entity (for placement preview) ===

    /**
     * Creates a translucent ghost entity for summon placement preview.
     * <p>
     * The ghost entity does not interact physically (sensor-only collider)
     * and is typically used to show where a summon will be placed.
     * </p>
     *
     * @param texturePath Path to the ghost texture.
     * @param scale       Scale factor for the preview.
     * @return The created ghost summon entity.
     */
    public static Entity createMeleeSummonGhost(String texturePath, float scale) {
        Entity ghost = new Entity()
                .addComponent(new TextureRenderComponent(texturePath));

        // 如果引擎支持透明度/染色，可以半透明显示（按你们的组件API改）
        // ghost.getComponent(TextureRenderComponent.class).setOpacity(0.5f);

        ghost.setScale(scale, scale);
        return ghost;
    }

}



